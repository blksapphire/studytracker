package com.mayowa.studytracker.presentation.goals

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.domain.repository.SessionRepository
import com.mayowa.studytracker.domain.usecase.CalculateAdaptiveGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class GoalsUiState(
    val adaptiveGoalMillis: Long = 0,
    val examDateMillis: Long? = null,
    val daysUntilExam: Int? = null
)

private val EXAM_DATE_KEY = longPreferencesKey("exam_date_millis")

@HiltViewModel
class GoalsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    calculateAdaptiveGoal: CalculateAdaptiveGoalUseCase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = combine(
        sessionRepository.getRecentDailyStats(7),
        dataStore.data.map { it[EXAM_DATE_KEY] }
    ) { stats, examDate ->
        val goal = calculateAdaptiveGoal(stats)
        val daysUntil = examDate?.let {
            TimeUnit.MILLISECONDS.toDays(it - System.currentTimeMillis()).toInt().coerceAtLeast(0)
        }
        GoalsUiState(adaptiveGoalMillis = goal, examDateMillis = examDate, daysUntilExam = daysUntil)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState())

    fun setExamDate(millis: Long) {
        viewModelScope.launch { dataStore.edit { it[EXAM_DATE_KEY] = millis } }
    }

    fun clearExamDate() {
        viewModelScope.launch { dataStore.edit { it.remove(EXAM_DATE_KEY) } }
    }
}
