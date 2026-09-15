package com.mayowa.studytracker.data.tracking

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mayowa.studytracker.R
import com.mayowa.studytracker.data.local.dao.SessionDao
import com.mayowa.studytracker.data.local.entity.SessionEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Foreground service. Polls UsageEvents every POLL_INTERVAL_MS, classifies
 * the current foreground app, and accumulates a live study session.
 *
 * The service also supports manual Start / Pause / Resume / Stop controls
 * from the dashboard. Pause keeps the live session but excludes paused time.
 */
@AndroidEntryPoint
class TrackingService : Service() {

    @Inject lateinit var sessionDao: SessionDao
    @Inject lateinit var appClassifier: AppClassifier

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pollingJob: Job? = null

    private var currentSessionId: Long? = null
    private var sessionStartedAt: Long = 0L
    private var lastNonDistractingAt: Long = 0L
    private var lastKnownForegroundPackage: String? = null
    private var pausedAt: Long = 0L
    private var manuallyStopped = false

    companion object {
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1
        const val POLL_INTERVAL_MS = 7_000L
        const val GRACE_WINDOW_MS = 8_000L

        const val ACTION_START = "com.mayowa.studytracker.action.START"
        const val ACTION_PAUSE = "com.mayowa.studytracker.action.PAUSE"
        const val ACTION_RESUME = "com.mayowa.studytracker.action.RESUME"
        const val ACTION_STOP = "com.mayowa.studytracker.action.STOP"

        private val _isPaused = MutableStateFlow(false)
        val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

        fun start(context: Context) {
            startWithAction(context, ACTION_START)
        }

        fun pause(context: Context) {
            startWithAction(context, ACTION_PAUSE)
        }

        fun resume(context: Context) {
            startWithAction(context, ACTION_RESUME)
        }

        fun stop(context: Context) {
            startWithAction(context, ACTION_STOP)
        }

        private fun startWithAction(context: Context, action: String) {
            val intent = Intent(context, TrackingService::class.java).setAction(action)
            context.startForegroundService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Tracking your study time"))
        pollingJob = serviceScope.launch { pollLoop() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> serviceScope.launch { startSession() }
            ACTION_PAUSE -> serviceScope.launch { pauseSession() }
            ACTION_RESUME -> serviceScope.launch { resumeSession() }
            ACTION_STOP -> serviceScope.launch { stopSession() }
        }
        return START_STICKY
    }

    private suspend fun pollLoop() {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        while (currentCoroutineContext().isActive) {
            if (!manuallyStopped && !_isPaused.value) {
                val foregroundPackage = getCurrentForegroundPackage(usm)
                handleForegroundChange(foregroundPackage)
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    private fun getCurrentForegroundPackage(usm: UsageStatsManager): String? {
        val end = System.currentTimeMillis()
        val start = end - (POLL_INTERVAL_MS * 3)
        val events = usm.queryEvents(start, end)
        var latestForeground: String? = null
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                latestForeground = event.packageName
            }
        }
        return latestForeground ?: lastKnownForegroundPackage
    }

    private suspend fun handleForegroundChange(foregroundPackage: String?) {
        lastKnownForegroundPackage = foregroundPackage
        val now = System.currentTimeMillis()
        val isDistracting = foregroundPackage?.let { appClassifier.isDistracting(it) } ?: false

        if (!isDistracting) {
            lastNonDistractingAt = now
            if (currentSessionId == null) {
                startSession(foregroundPackage)
            } else {
                updateLiveSession(now, foregroundPackage)
            }
        } else {
            val idleTooLong = (now - lastNonDistractingAt) > GRACE_WINDOW_MS
            if (idleTooLong && currentSessionId != null) {
                finishSession(now)
            }
        }
    }

    private suspend fun startSession(foregroundPackage: String? = lastKnownForegroundPackage) {
        manuallyStopped = false
        _isPaused.value = false
        pausedAt = 0L

        if (currentSessionId != null) return

        val existing = sessionDao.getLiveSession()
        if (existing != null) {
            currentSessionId = existing.id
            sessionStartedAt = existing.startedAt
            return
        }

        val now = System.currentTimeMillis()
        sessionStartedAt = now
        lastNonDistractingAt = now
        currentSessionId = sessionDao.upsertSession(
            SessionEntity(
                startedAt = now,
                endedAt = null,
                durationMillis = 0,
                tag = null,
                appTrailCsv = foregroundPackage.orEmpty()
            )
        )
    }

    private suspend fun updateLiveSession(now: Long, foregroundPackage: String?) {
        val id = currentSessionId ?: return
        val existing = sessionDao.getLiveSession()
        sessionDao.upsertSession(
            SessionEntity(
                id = id,
                startedAt = sessionStartedAt,
                endedAt = null,
                durationMillis = now - sessionStartedAt,
                tag = existing?.tag,
                appTrailCsv = foregroundPackage.orEmpty()
            )
        )
    }

    private suspend fun pauseSession() {
        if (currentSessionId == null || _isPaused.value) return
        pausedAt = System.currentTimeMillis()
        val existing = sessionDao.getLiveSession()
        val duration = (pausedAt - sessionStartedAt).coerceAtLeast(0L)
        existing?.let {
            sessionDao.upsertSession(
                it.copy(durationMillis = duration)
            )
        }
        _isPaused.value = true
    }

    private suspend fun resumeSession() {
        manuallyStopped = false
        if (!_isPaused.value) {
            startSession()
            return
        }

        val now = System.currentTimeMillis()
        sessionStartedAt += (now - pausedAt).coerceAtLeast(0L)
        pausedAt = 0L
        _isPaused.value = false
        lastNonDistractingAt = now
    }

    private suspend fun stopSession() {
        val now = System.currentTimeMillis()
        if (currentSessionId != null) {
            val existing = sessionDao.getLiveSession()
            val duration = if (_isPaused.value) {
                existing?.durationMillis ?: 0L
            } else {
                (now - sessionStartedAt).coerceAtLeast(0L)
            }
            existing?.let {
                sessionDao.upsertSession(
                    it.copy(endedAt = now, durationMillis = duration)
                )
            }
            RollupScheduler.triggerNow(applicationContext)
        }

        currentSessionId = null
        sessionStartedAt = 0L
        pausedAt = 0L
        _isPaused.value = false
        manuallyStopped = true
        stopSelf()
    }

    private suspend fun finishSession(now: Long) {
        val id = currentSessionId ?: return
        val existing = sessionDao.getLiveSession()
        sessionDao.upsertSession(
            SessionEntity(
                id = id,
                startedAt = sessionStartedAt,
                endedAt = now,
                durationMillis = (now - sessionStartedAt).coerceAtLeast(0L),
                tag = existing?.tag,
                appTrailCsv = ""
            )
        )
        currentSessionId = null
        RollupScheduler.triggerNow(applicationContext)
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StudyTracker")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Study Tracking", NotificationManager.IMPORTANCE_MIN
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        pollingJob?.cancel()
        serviceScope.cancel()
        _isPaused.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
