package com.mayowa.studytracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.domain.model.DailyStats
import com.mayowa.studytracker.domain.model.StudySession
import com.mayowa.studytracker.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    sessionRepository: SessionRepository
) : ViewModel() {
    val dailyStats: StateFlow<List<DailyStats>> = sessionRepository.getRecentDailyStats(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<StudySession>> = sessionRepository.getRecentSessions(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isEmpty: StateFlow<Boolean> = combine(dailyStats, sessions) { days, recentSessions ->
        days.isEmpty() && recentSessions.isEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
}
