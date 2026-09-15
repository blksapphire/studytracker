package com.mayowa.studytracker.presentation.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.data.profile.ProfileStore
import com.mayowa.studytracker.domain.repository.SessionRepository
import com.mayowa.studytracker.domain.usecase.BuildStudyPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StudyPlanViewModel @Inject constructor(
    profileStore: ProfileStore,
    repository: SessionRepository,
    private val buildPlan: BuildStudyPlanUseCase
) : ViewModel() {
    val plan: StateFlow<com.mayowa.studytracker.domain.usecase.StudyPlan> = combine(
        profileStore.profile,
        repository.getRecentDailyStats(14)
    ) { profile, stats -> buildPlan(profile.subjects, profile.dailyTargetMinutes, stats) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), buildPlan(emptyList(), 60, emptyList()))
}
