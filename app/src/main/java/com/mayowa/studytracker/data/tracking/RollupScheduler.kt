package com.mayowa.studytracker.data.tracking

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object RollupScheduler {
    private const val PERIODIC_WORK_NAME = "daily_rollup_periodic"
    private const val ONE_SHOT_WORK_NAME = "daily_rollup_one_shot"

    /** Call once from Application.onCreate(). */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyRollupWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    /** Call right after a session ends, so numbers update within seconds instead of waiting for the next 15-min tick. */
    fun triggerNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<DailyRollupWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_SHOT_WORK_NAME, ExistingWorkPolicy.REPLACE, request
        )
    }
}
