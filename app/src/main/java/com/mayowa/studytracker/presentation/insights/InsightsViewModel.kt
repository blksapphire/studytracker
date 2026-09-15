package com.mayowa.studytracker.presentation.insights

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class AppInsight(val packageName: String, val label: String, val trackedMillis: Long)
data class DayInsight(val date: String, val millis: Long)

data class WeeklyInsights(
    val totalMillis: Long = 0, val averageMillisPerDay: Long = 0,
    val bestDay: String? = null, val bestDayMillis: Long = 0,
    val topTag: String? = null, val sessionCount: Int = 0,
    val averageSessionMillis: Long = 0, val peakHour: Int? = null,
    val topApps: List<AppInsight> = emptyList(), val days: List<DayInsight> = emptyList(),
    val distractionTotal: Int = 0
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val weeklyInsights: StateFlow<WeeklyInsights> = combine(
        sessionRepository.getRecentDailyStats(7), sessionRepository.getRecentSessions(100)
    ) { week, sessions ->
        if (week.isEmpty() && sessions.isEmpty()) return@combine WeeklyInsights()
        val best = week.maxByOrNull { it.totalStudyMillis }
        val tagCounts = week.mapNotNull { it.topTag }.groupingBy { it }.eachCount()
        val cutoff = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        val weekSessions = sessions.filter { it.startedAt >= cutoff && it.endedAt != null }
        val peakHour = weekSessions.groupingBy { Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).hour }.eachCount().maxByOrNull { it.value }?.key
        val appCounts = weekSessions.flatMap { it.appTrail }.filter { it.isNotBlank() }.groupingBy { it }.eachCount()
        val topApps = appCounts.entries.sortedByDescending { it.value }.take(5).map { (pkg, samples) -> AppInsight(pkg, applicationLabel(pkg), samples * 7_000L) }
        WeeklyInsights(
            totalMillis = week.sumOf { it.totalStudyMillis },
            averageMillisPerDay = if (week.isEmpty()) 0 else week.sumOf { it.totalStudyMillis } / week.size,
            bestDay = best?.date, bestDayMillis = best?.totalStudyMillis ?: 0,
            topTag = tagCounts.maxByOrNull { it.value }?.key, sessionCount = weekSessions.size,
            averageSessionMillis = if (weekSessions.isEmpty()) 0 else weekSessions.sumOf { it.durationMillis } / weekSessions.size,
            peakHour = peakHour, topApps = topApps,
            days = week.sortedBy { it.date }.map { DayInsight(it.date, it.totalStudyMillis) },
            distractionTotal = weekSessions.sumOf { it.distractionCount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyInsights())

    private fun applicationLabel(packageName: String): String = runCatching {
        context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName, 0)).toString()
    }.getOrDefault(packageName)
}
