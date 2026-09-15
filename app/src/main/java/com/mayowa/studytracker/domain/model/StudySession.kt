package com.mayowa.studytracker.domain.model

/**
 * A single contiguous block of study time.
 */
data class StudySession(
    val id: Long = 0,
    val startedAt: Long,
    val endedAt: Long?,
    val durationMillis: Long,
    val tag: String? = null,
    val appTrail: List<String> = emptyList(),
    val distractionCount: Int = 0
)

data class DailyStats(
    val date: String,
    val totalStudyMillis: Long,
    val longestSessionMillis: Long,
    val sessionCount: Int,
    val topTag: String? = null
)

data class StreakInfo(
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val lastActiveDate: String?
)
