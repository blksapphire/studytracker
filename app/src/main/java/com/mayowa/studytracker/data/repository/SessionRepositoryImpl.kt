package com.mayowa.studytracker.data.repository

import com.mayowa.studytracker.data.local.dao.DailyStatsDao
import com.mayowa.studytracker.data.local.dao.SessionDao
import com.mayowa.studytracker.data.local.entity.DailyStatsEntity
import com.mayowa.studytracker.data.local.entity.SessionEntity
import com.mayowa.studytracker.domain.model.DailyStats
import com.mayowa.studytracker.domain.model.StudySession
import com.mayowa.studytracker.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val dailyStatsDao: DailyStatsDao
) : SessionRepository {

    override fun getRecentSessions(limit: Int): Flow<List<StudySession>> =
        sessionDao.getRecentSessions(limit).map { list -> list.map { it.toDomain() } }

    override fun getRecentDailyStats(limit: Int): Flow<List<DailyStats>> =
        dailyStatsDao.getRecentDailyStats(limit).map { list -> list.map { it.toDomain() } }

    override fun getLiveSession(): Flow<StudySession?> =
        sessionDao.getLiveSessionFlow().map { it?.toDomain() }

    override suspend fun tagSession(sessionId: Long, tag: String) =
        sessionDao.tagSession(sessionId, tag)

    private fun SessionEntity.toDomain() = StudySession(
        id = id,
        startedAt = startedAt,
        endedAt = endedAt,
        durationMillis = durationMillis,
        tag = tag,
        appTrail = appTrailCsv.split(",").filter { it.isNotBlank() },
        distractionCount = distractionCount
    )

    private fun DailyStatsEntity.toDomain() = DailyStats(
        date = date,
        totalStudyMillis = totalStudyMillis,
        longestSessionMillis = longestSessionMillis,
        sessionCount = sessionCount,
        topTag = topTag
    )
}
