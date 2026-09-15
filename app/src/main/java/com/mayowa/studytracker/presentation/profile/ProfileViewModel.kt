package com.mayowa.studytracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.data.profile.ProfileStore
import com.mayowa.studytracker.domain.repository.SessionRepository
import com.mayowa.studytracker.domain.usecase.CalculateStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

 data class ProfileUiState(
    val name: String = "",
    val goal: String = "",
    val subjects: List<String> = emptyList(),
    val dailyTargetMinutes: Int = 60,
    val examDateMillis: Long? = null,
    val totalStudyMillis: Long = 0,
    val sessions: Int = 0,
    val streakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val xp: Int = 0,
    val level: Int = 1
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    profileStore: ProfileStore,
    sessionRepository: SessionRepository,
    calculateStreak: CalculateStreakUseCase
) : ViewModel() {
    val uiState: StateFlow<ProfileUiState> = combine(
        profileStore.profile,
        sessionRepository.getRecentDailyStats(365),
        sessionRepository.getRecentSessions(500)
    ) { profile, stats, sessions ->
        val total = stats.sumOf { it.totalStudyMillis }
        val xp = ((total / 300_000L).toInt() + sessions.count { it.endedAt != null } * 10).coerceAtLeast(0)
        val level = (xp / 250) + 1
        val streak = calculateStreak(stats)
        ProfileUiState(
            name = profile.name,
            goal = profile.goal,
            subjects = profile.subjects,
            dailyTargetMinutes = profile.dailyTargetMinutes,
            examDateMillis = profile.examDateMillis,
            totalStudyMillis = total,
            sessions = sessions.count { it.endedAt != null },
            streakDays = streak.currentStreakDays,
            bestStreakDays = streak.longestStreakDays,
            xp = xp,
            level = level
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())
}
