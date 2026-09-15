package com.mayowa.studytracker.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class WeeklyInsights(
    val totalMillis: Long = 0,
    val averageMillisPerDay: Long = 0,
    val bestDay: String? = null,
    val bestDayMillis: Long = 0,
    val topTag: String? = null,
    val sessionCount: Int = 0,
    val averageSessionMillis: Long = 0,
    val peakHour: Int? = null
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    sessionRepository: SessionRepository
) : ViewModel() {

    val weeklyInsights: StateFlow<WeeklyInsights> = combine(
        sessionRepository.getRecentDailyStats(7),
        sessionRepository.getRecentSessions(100)
    ) { week, sessions ->
        if (week.isEmpty() && sessions.isEmpty()) return@combine WeeklyInsights()

        val best = week.maxByOrNull { it.totalStudyMillis }
        val tagCounts = week.mapNotNull { it.topTag }.groupingBy { it }.eachCount()
        val cutoff = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        val weekSessions = sessions.filter { it.startedAt >= cutoff && it.endedAt != null }
        val peakHour = weekSessions
            .groupingBy {
                Instant.ofEpochMilli(it.startedAt)
                    .atZone(ZoneId.systemDefault())
                    .hour
            }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        WeeklyInsights(
            totalMillis = week.sumOf { it.totalStudyMillis },
            averageMillisPerDay = if (week.isEmpty()) 0 else week.sumOf { it.totalStudyMillis } / week.size,
            bestDay = best?.date,
            bestDayMillis = best?.totalStudyMillis ?: 0,
            topTag = tagCounts.maxByOrNull { it.value }?.key,
            sessionCount = weekSessions.size,
            averageSessionMillis = if (weekSessions.isEmpty()) 0 else weekSessions.sumOf { it.durationMillis } / weekSessions.size,
            peakHour = peakHour
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyInsights())
}
