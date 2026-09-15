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

@AndroidEntryPoint
class TrackingService : Service() {

    @Inject lateinit var sessionDao: SessionDao
    @Inject lateinit var appClassifier: AppClassifier

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pollingJob: Job? = null
    private var liveTimerJob: Job? = null
    private var focusTimerJob: Job? = null

    private var currentSessionId: Long? = null
    private var sessionStartedAt: Long = 0L
    private var lastNonDistractingAt: Long = 0L
    private var lastKnownForegroundPackage: String? = null
    private var lastDistractingPackage: String? = null
    private var pausedAt: Long = 0L
    private var manuallyStopped = false

    companion object {
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1
        const val DISTRACTION_NOTIFICATION_ID = 2
        const val POLL_INTERVAL_MS = 7_000L
        const val GRACE_WINDOW_MS = 8_000L
        const val DEFAULT_FOCUS_DURATION_MS = 25 * 60 * 1000L

        const val ACTION_START = "com.mayowa.studytracker.action.START"
        const val ACTION_PAUSE = "com.mayowa.studytracker.action.PAUSE"
        const val ACTION_RESUME = "com.mayowa.studytracker.action.RESUME"
        const val ACTION_STOP = "com.mayowa.studytracker.action.STOP"
        const val ACTION_FOCUS_START = "com.mayowa.studytracker.action.FOCUS_START"
        const val ACTION_FOCUS_STOP = "com.mayowa.studytracker.action.FOCUS_STOP"

        private val _isPaused = MutableStateFlow(false)
        val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

        private val _liveElapsedMillis = MutableStateFlow(0L)
        val liveElapsedMillis: StateFlow<Long> = _liveElapsedMillis.asStateFlow()

        private val _focusRemainingMillis = MutableStateFlow(0L)
        val focusRemainingMillis: StateFlow<Long> = _focusRemainingMillis.asStateFlow()

        private val _isFocusMode = MutableStateFlow(false)
        val isFocusMode: StateFlow<Boolean> = _isFocusMode.asStateFlow()

        fun start(context: Context) = startWithAction(context, ACTION_START)
        fun pause(context: Context) = startWithAction(context, ACTION_PAUSE)
        fun resume(context: Context) = startWithAction(context, ACTION_RESUME)
        fun stop(context: Context) = startWithAction(context, ACTION_STOP)
        fun startFocus(context: Context) = startWithAction(context, ACTION_FOCUS_START)
        fun stopFocus(context: Context) = startWithAction(context, ACTION_FOCUS_STOP)

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
        liveTimerJob = serviceScope.launch { liveTimerLoop() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> serviceScope.launch { startSession() }
            ACTION_PAUSE -> serviceScope.launch { pauseSession() }
            ACTION_RESUME -> serviceScope.launch { resumeSession() }
            ACTION_STOP -> serviceScope.launch { stopSession() }
            ACTION_FOCUS_START -> serviceScope.launch { startFocusMode() }
            ACTION_FOCUS_STOP -> serviceScope.launch { stopFocusMode() }
        }
        return START_STICKY
    }

    private suspend fun pollLoop() {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        while (currentCoroutineContext().isActive) {
            if (!manuallyStopped && !_isPaused.value) {
                handleForegroundChange(getCurrentForegroundPackage(usm))
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    private suspend fun liveTimerLoop() {
        while (currentCoroutineContext().isActive) {
            val sessionId = currentSessionId
            if (sessionId != null) {
                _liveElapsedMillis.value = if (_isPaused.value) {
                    sessionDao.getLiveSession()?.durationMillis ?: _liveElapsedMillis.value
                } else {
                    (System.currentTimeMillis() - sessionStartedAt).coerceAtLeast(0L)
                }
            } else {
                _liveElapsedMillis.value = 0L
            }
            delay(1_000L)
        }
    }

    private suspend fun startFocusMode() {
        manuallyStopped = false
        startSession()
        if (_isFocusMode.value) return
        _isFocusMode.value = true
        _focusRemainingMillis.value = DEFAULT_FOCUS_DURATION_MS
        focusTimerJob?.cancel()
        focusTimerJob = serviceScope.launch {
            while (currentCoroutineContext().isActive && _focusRemainingMillis.value > 0L) {
                delay(1_000L)
                _focusRemainingMillis.value = (_focusRemainingMillis.value - 1_000L).coerceAtLeast(0L)
            }
            if (currentCoroutineContext().isActive && _isFocusMode.value) {
                _isFocusMode.value = false
                _focusRemainingMillis.value = 0L
                updateTrackingNotification("Focus session complete")
            }
        }
        updateTrackingNotification("Focus mode: 25 minutes")
    }

    private fun stopFocusMode() {
        focusTimerJob?.cancel()
        focusTimerJob = null
        _isFocusMode.value = false
        _focusRemainingMillis.value = 0L
        updateTrackingNotification("Tracking your study time")
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
        val now = System.currentTimeMillis()
        val isDistracting = foregroundPackage?.let { appClassifier.isDistracting(it) } ?: false

        if (!isDistracting) {
            lastNonDistractingAt = now
            lastDistractingPackage = null
            if (currentSessionId == null) startSession(foregroundPackage)
            else updateLiveSession(now, foregroundPackage)
        } else {
            if (foregroundPackage != null && foregroundPackage != lastDistractingPackage) {
                recordDistraction(foregroundPackage)
                lastDistractingPackage = foregroundPackage
            }
            val idleTooLong = (now - lastNonDistractingAt) > GRACE_WINDOW_MS
            if (idleTooLong && currentSessionId != null && !_isFocusMode.value) finishSession(now)
            else if (currentSessionId != null && _isFocusMode.value) updateLiveSession(now, null)
        }
        lastKnownForegroundPackage = foregroundPackage
    }

    private suspend fun recordDistraction(packageName: String) {
        val existing = sessionDao.getLiveSession() ?: return
        sessionDao.upsertSession(existing.copy(distractionCount = existing.distractionCount + 1))
        if (_isFocusMode.value) {
            showDistractionNudge(packageName)
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
            _liveElapsedMillis.value = existing.durationMillis
            return
        }

        val now = System.currentTimeMillis()
        sessionStartedAt = now
        lastNonDistractingAt = now
        _liveElapsedMillis.value = 0L
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
        val existing = sessionDao.getLiveSession() ?: return
        val duration = (now - sessionStartedAt).coerceAtLeast(0L)
        _liveElapsedMillis.value = duration

        val packageName = foregroundPackage.orEmpty()
        val trail = if (packageName.isBlank()) existing.appTrailCsv
        else if (existing.appTrailCsv.isBlank()) packageName
        else existing.appTrailCsv + "," + packageName

        sessionDao.upsertSession(existing.copy(id = id, durationMillis = duration, appTrailCsv = trail))
    }

    private suspend fun pauseSession() {
        if (currentSessionId == null || _isPaused.value) return
        pausedAt = System.currentTimeMillis()
        val existing = sessionDao.getLiveSession()
        val duration = (pausedAt - sessionStartedAt).coerceAtLeast(0L)
        existing?.let { sessionDao.upsertSession(it.copy(durationMillis = duration)) }
        _liveElapsedMillis.value = duration
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
        stopFocusMode()
        val now = System.currentTimeMillis()
        if (currentSessionId != null) {
            val existing = sessionDao.getLiveSession()
            val duration = if (_isPaused.value) existing?.durationMillis ?: _liveElapsedMillis.value
            else (now - sessionStartedAt).coerceAtLeast(0L)
            existing?.let { sessionDao.upsertSession(it.copy(endedAt = now, durationMillis = duration)) }
            RollupScheduler.triggerNow(applicationContext)
        }
        currentSessionId = null
        sessionStartedAt = 0L
        pausedAt = 0L
        _liveElapsedMillis.value = 0L
        _isPaused.value = false
        manuallyStopped = true
        stopSelf()
    }

    private suspend fun finishSession(now: Long) {
        val id = currentSessionId ?: return
        val existing = sessionDao.getLiveSession()
        val duration = (now - sessionStartedAt).coerceAtLeast(0L)
        existing?.let { sessionDao.upsertSession(it.copy(id = id, endedAt = now, durationMillis = duration)) }
        currentSessionId = null
        _liveElapsedMillis.value = 0L
        RollupScheduler.triggerNow(applicationContext)
    }

    private fun showDistractionNudge(packageName: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stay focused")
            .setContentText("You opened a distracting app during Focus Mode.")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java).notify(DISTRACTION_NOTIFICATION_ID, notification)
    }

    private fun updateTrackingNotification(text: String) {
        getSystemService(NotificationManager::class.java).notify(
            NOTIFICATION_ID,
            buildNotification(text)
        )
    }

    private fun buildNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StudyTracker")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Study Tracking", NotificationManager.IMPORTANCE_MIN)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        pollingJob?.cancel()
        liveTimerJob?.cancel()
        focusTimerJob?.cancel()
        serviceScope.cancel()
        _isPaused.value = false
        _liveElapsedMillis.value = 0L
        _isFocusMode.value = false
        _focusRemainingMillis.value = 0L
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
