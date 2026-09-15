package com.mayowa.studytracker.data.local.dao

import androidx.room.*
import com.mayowa.studytracker.data.local.entity.DailyStatsEntity
import com.mayowa.studytracker.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    // Upsert on every tick while a session is live, not just on end —
    // guards against losing data if the foreground service gets killed.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions WHERE endedAt IS NULL LIMIT 1")
    suspend fun getLiveSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE endedAt IS NULL LIMIT 1")
    fun getLiveSessionFlow(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE startedAt BETWEEN :dayStart AND :dayEnd ORDER BY startedAt DESC")
    fun getSessionsForDay(dayStart: Long, dayEnd: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 100): Flow<List<SessionEntity>>

    @Query("UPDATE sessions SET tag = :tag WHERE id = :sessionId")
    suspend fun tagSession(sessionId: Long, tag: String)
}

@Dao
interface DailyStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyStats(stats: DailyStatsEntity)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getStatsForDate(date: String): DailyStatsEntity?

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT :limit")
    fun getRecentDailyStats(limit: Int = 30): Flow<List<DailyStatsEntity>>
}
