package com.mayowa.studytracker.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.data.profile.ProfileStore
import com.mayowa.studytracker.domain.repository.SessionRepository
import com.mayowa.studytracker.domain.usecase.CalculateAdaptiveGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class StudyPlanItem(
    val subject: String,
    val minutes: Int,
    val reason: String
)

data class GoalsUiState(
    val adaptiveGoalMillis: Long = 0,
    val examDateMillis: Long? = null,
    val daysUntilExam: Int? = null,
    val dailyTargetMinutes: Int = 60,
    val subjects: List<String> = emptyList(),
    val plan: List<StudyPlanItem> = emptyList()
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    calculateAdaptiveGoal: CalculateAdaptiveGoalUseCase,
    private val profileStore: ProfileStore
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = combine(
        sessionRepository.getRecentDailyStats(7),
        profileStore.profile
    ) { stats, profile ->
        val goal = calculateAdaptiveGoal(stats)
        val examDate = profile.examDateMillis
        val daysUntil = examDate?.let {
            TimeUnit.MILLISECONDS.toDays(it - System.currentTimeMillis()).toInt().coerceAtLeast(0)
        }
        val target = if (profile.dailyTargetMinutes > 0) profile.dailyTargetMinutes else (goal / 60_000L).toInt().coerceAtLeast(15)
        val subjects = profile.subjects
        val count = subjects.size.coerceAtLeast(1)
        val base = target / count
        val remainder = target % count
        val plan = subjects.mapIndexed { index, subject ->
            StudyPlanItem(
                subject = subject,
                minutes = base + if (index < remainder) 1 else 0,
                reason = when {
                    daysUntil != null && daysUntil <= 7 -> "Exam is close"
                    stats.isNotEmpty() -> "Based on your recent rhythm"
                    else -> "Balanced starting plan"
                }
            )
        }
        GoalsUiState(goal, examDate, daysUntil, target, subjects, plan)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    fun setExamDate(millis: Long) {
        viewModelScope.launch { profileStore.setExamDate(millis) }
    }

    fun clearExamDate() {
        viewModelScope.launch { profileStore.setExamDate(null) }
    }
}
