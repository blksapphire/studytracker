package com.mayowa.studytracker.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
    val isLive: Boolean = false,
    val elapsedMillis: Long = 0,
    val currentTag: String? = null
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val uiState: StateFlow<SessionUiState> = sessionRepository.getLiveSession()
        .map { session ->
            if (session == null) SessionUiState()
            else SessionUiState(
                isLive = true,
                elapsedMillis = session.durationMillis,
                currentTag = session.tag
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionUiState())

    fun tagCurrentSession(tag: String) {
        viewModelScope.launch {
            val liveId = sessionRepository.getLiveSession().first()?.id ?: return@launch
            sessionRepository.tagSession(liveId, tag)
        }
    }
}
