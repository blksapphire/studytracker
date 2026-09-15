package com.mayowa.studytracker.data.tracking

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mayowa.studytracker.data.local.dao.DailyStatsDao
import com.mayowa.studytracker.data.local.dao.SessionDao
import com.mayowa.studytracker.data.local.entity.DailyStatsEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.first

/**
 * Rolls today's sessions into a single DailyStatsEntity row. Runs
 * periodically (every 15 min, WorkManager's practical minimum) plus once
 * whenever a session ends, so Dashboard/History/Widget never read stale
 * numbers by more than a few minutes.
 */
@HiltWorker
class DailyRollupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sessionDao: SessionDao,
    private val dailyStatsDao: DailyStatsDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = dateFormat.format(Date())

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val dayStart = calendar.timeInMillis
        val dayEnd = dayStart + 24 * 60 * 60 * 1000L

        // getSessionsForDay is a Flow in the DAO for UI observation; for a
        // one-shot rollup we grab the first emission.
        val sessions = sessionDao.getSessionsForDay(dayStart, dayEnd)
        var total = 0L
        var longest = 0L
        var count = 0
        val tagCounts = mutableMapOf<String, Int>()

        // Simplified one-shot collection — swap for a suspend DAO query
        // (non-Flow) once this is wired up for real; kept as Flow here so
        // the same DAO method also serves live UI observation.
        sessions.first().forEach { session ->
            total += session.durationMillis
            if (session.durationMillis > longest) longest = session.durationMillis
            count++
            session.tag?.let { tagCounts[it] = (tagCounts[it] ?: 0) + 1 }
        }

        dailyStatsDao.upsertDailyStats(
            DailyStatsEntity(
                date = today,
                totalStudyMillis = total,
                longestSessionMillis = longest,
                sessionCount = count,
                topTag = tagCounts.maxByOrNull { it.value }?.key
            )
        )

        return Result.success()
    }
}
