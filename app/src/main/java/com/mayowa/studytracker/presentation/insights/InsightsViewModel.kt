package com.mayowa.studytracker.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class WeeklyInsights(
    val totalMillis: Long = 0,
    val averageMillisPerDay: Long = 0,
    val bestDay: String? = null,
    val bestDayMillis: Long = 0,
    val topTag: String? = null
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    sessionRepository: SessionRepository
) : ViewModel() {

    val weeklyInsights: StateFlow<WeeklyInsights> = sessionRepository.getRecentDailyStats(7)
        .map { week ->
            if (week.isEmpty()) return@map WeeklyInsights()
            val best = week.maxByOrNull { it.totalStudyMillis }
            val tagCounts = week.mapNotNull { it.topTag }.groupingBy { it }.eachCount()
            WeeklyInsights(
                totalMillis = week.sumOf { it.totalStudyMillis },
                averageMillisPerDay = week.sumOf { it.totalStudyMillis } / week.size,
                bestDay = best?.date,
                bestDayMillis = best?.totalStudyMillis ?: 0,
                topTag = tagCounts.maxByOrNull { it.value }?.key
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyInsights())
}
