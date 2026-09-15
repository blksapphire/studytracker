package com.mayowa.studytracker.domain.model

/**
 * A single contiguous block of "study" time — screen on, foreground app
 * not classified as distracting, no grace-window break longer than the
 * configured threshold.
 */
data class StudySession(
    val id: Long = 0,
    val startedAt: Long,        // epoch millis
    val endedAt: Long?,         // null while session is still live
    val durationMillis: Long,
    val tag: String? = null,    // e.g. "Calculus", "MCAT Prep" — user-assigned subject tag
    val appTrail: List<String> = emptyList() // package names touched during session, for insights
)

/** One day's rolled-up totals — written by the WorkManager rollup job, read by Dashboard/History/Widget. */
data class DailyStats(
    val date: String,               // ISO yyyy-MM-dd
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
