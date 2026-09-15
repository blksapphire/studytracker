package com.mayowa.studytracker.domain.usecase

import com.mayowa.studytracker.domain.model.DailyStats
import kotlin.math.roundToLong

/**
 * Suggests tomorrow's goal from a rolling average of recent days rather
 * than a fixed arbitrary number. Nudges up slightly (10%) on a hot streak
 * so the goal grows with the user instead of staying static once they've
 * clearly outgrown it, and never nudges below a sane floor.
 */
class CalculateAdaptiveGoalUseCase {

    operator fun invoke(
        recentStats: List<DailyStats>, // last 7-14 days, most recent first
        windowSize: Int = 7,
        floorMillis: Long = 15 * 60 * 1000L,
        growthNudgeStreakDays: Int = 5
    ): Long {
        val window = recentStats.take(windowSize).filter { it.totalStudyMillis > 0 }
        if (window.isEmpty()) return floorMillis

        val average = window.sumOf { it.totalStudyMillis } / window.size

        // If the user has hit their average for several consecutive days,
        // nudge the goal up 10% — otherwise a fast-improving user stays
        // stuck at a goal that stopped being a stretch weeks ago.
        val recentHits = recentStats.take(growthNudgeStreakDays)
        val onAHotStreak = recentHits.size == growthNudgeStreakDays &&
            recentHits.all { it.totalStudyMillis >= average }

        val goal = if (onAHotStreak) (average * 1.1).roundToLong() else average
        return maxOf(goal, floorMillis)
    }
}
