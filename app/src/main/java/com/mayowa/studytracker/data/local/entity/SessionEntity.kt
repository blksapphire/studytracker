package com.mayowa.studytracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long?,
    val durationMillis: Long,
    val tag: String?,
    val appTrailCsv: String // comma-separated package names; kept simple, no TypeConverters needed yet
)

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val date: String, // yyyy-MM-dd
    val totalStudyMillis: Long,
    val longestSessionMillis: Long,
    val sessionCount: Int,
    val topTag: String?
)
