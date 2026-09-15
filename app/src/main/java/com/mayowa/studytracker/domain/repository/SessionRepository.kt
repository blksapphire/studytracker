package com.mayowa.studytracker.domain.repository

import com.mayowa.studytracker.domain.model.DailyStats
import com.mayowa.studytracker.domain.model.StudySession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getRecentSessions(limit: Int = 100): Flow<List<StudySession>>
    fun getRecentDailyStats(limit: Int = 30): Flow<List<DailyStats>>
    fun getLiveSession(): Flow<StudySession?>
    suspend fun tagSession(sessionId: Long, tag: String)
}
