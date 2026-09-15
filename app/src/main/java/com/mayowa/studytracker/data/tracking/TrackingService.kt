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
import javax.inject.Inject

/**
 * Foreground service. Polls UsageEvents every POLL_INTERVAL_MS, classifies
 * the current foreground app, and accumulates a live "study session" while
 * the screen is on and the foreground app isn't in the distracting set.
 *
 * A GRACE_WINDOW_MS buffer prevents a quick app-switch (checking a text)
 * from prematurely ending a session.
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

    companion object {
        const val CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1
        const val POLL_INTERVAL_MS = 7_000L
        const val GRACE_WINDOW_MS = 8_000L

        fun start(context: Context) {
            context.startForegroundService(Intent(context, TrackingService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Tracking your study time"))
        pollingJob = serviceScope.launch { pollLoop() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private suspend fun pollLoop() {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        while (isActive) {
            val foregroundPackage = getCurrentForegroundPackage(usm)
            handleForegroundChange(foregroundPackage)
            delay(POLL_INTERVAL_MS)
        }
    }

    private fun getCurrentForegroundPackage(usm: UsageStatsManager): String? {
        val end = System.currentTimeMillis()
        val start = end - (POLL_INTERVAL_MS * 3) // small overlap window, avoids missing fast switches
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
                sessionStartedAt = now
                currentSessionId = sessionDao.upsertSession(
                    SessionEntity(
                        startedAt = now,
                        endedAt = null,
                        durationMillis = 0,
                        tag = null,
                        appTrailCsv = foregroundPackage.orEmpty()
                    )
                )
            } else {
                // Live-update duration so a service kill doesn't lose the session
                sessionDao.upsertSession(
                    SessionEntity(
                        id = currentSessionId!!,
                        startedAt = sessionStartedAt,
                        endedAt = null,
                        durationMillis = now - sessionStartedAt,
                        tag = null,
                        appTrailCsv = foregroundPackage.orEmpty()
                    )
                )
            }
        } else {
            // Only end the session once we're past the grace window —
            // a brief check of a distracting app shouldn't kill a streak.
            val idleTooLong = (now - lastNonDistractingAt) > GRACE_WINDOW_MS
            if (idleTooLong && currentSessionId != null) {
                sessionDao.upsertSession(
                    SessionEntity(
                        id = currentSessionId!!,
                        startedAt = sessionStartedAt,
                        endedAt = now,
                        durationMillis = now - sessionStartedAt,
                        tag = null,
                        appTrailCsv = ""
                    )
                )
                currentSessionId = null
                RollupScheduler.triggerNow(applicationContext)
            }
        }
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
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
