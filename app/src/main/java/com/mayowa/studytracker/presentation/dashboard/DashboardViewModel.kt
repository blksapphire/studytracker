package com.mayowa.studytracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.domain.repository.SessionRepository
import com.mayowa.studytracker.domain.usecase.CalculateAdaptiveGoalUseCase
import com.mayowa.studytracker.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DashboardUiState(
    val todayStudyMillis: Long = 0,
    val goalMillis: Long = 0,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val isSessionLive: Boolean = false,
    val recentTags: List<String> = emptyList(),
    val weeklyStudyMillis: Long = 0,
    val weeklySessionCount: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    calculateStreak: CalculateStreakUseCase,
    calculateAdaptiveGoal: CalculateAdaptiveGoalUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        sessionRepository.getRecentDailyStats(14),
        sessionRepository.getRecentSessions(100),
        sessionRepository.getLiveSession()
    ) { stats, sessions, liveSession ->
        val today = stats.firstOrNull()
        val streak = calculateStreak(stats)
        val goal = calculateAdaptiveGoal(stats.drop(1))
        val weekStats = stats.take(7)
        val cutoff = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        val weeklySessions = sessions.count { it.startedAt >= cutoff && it.endedAt != null }

        DashboardUiState(
            todayStudyMillis = today?.totalStudyMillis ?: 0,
            goalMillis = goal,
            currentStreakDays = streak.currentStreakDays,
            longestStreakDays = streak.longestStreakDays,
            isSessionLive = liveSession != null,
            recentTags = listOfNotNull(today?.topTag),
            weeklyStudyMillis = weekStats.sumOf { it.totalStudyMillis },
            weeklySessionCount = weeklySessions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
