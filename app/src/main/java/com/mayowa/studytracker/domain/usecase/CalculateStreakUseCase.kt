package com.mayowa.studytracker.domain.usecase

import com.mayowa.studytracker.domain.model.DailyStats
import com.mayowa.studytracker.domain.model.StreakInfo
import java.text.SimpleDateFormat
import java.util.*

/**
 * A day "counts" toward the streak if totalStudyMillis clears the given
 * threshold (default 10 min — low enough that one lazy day doesn't feel
 * punishing, high enough that opening the app for 30s doesn't count).
 *
 * Pure function over already-fetched DailyStats — no DB/Android deps,
 * so this is unit-testable without Robolectric or an emulator.
 */
class CalculateStreakUseCase {

    operator fun invoke(
        dailyStats: List<DailyStats>, // expected sorted descending by date
        thresholdMillis: Long = 10 * 60 * 1000L
    ): StreakInfo {
        if (dailyStats.isEmpty()) return StreakInfo(0, 0, null)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val qualifyingDates = dailyStats
            .filter { it.totalStudyMillis >= thresholdMillis }
            .map { dateFormat.parse(it.date)!! }
            .sortedDescending()

        if (qualifyingDates.isEmpty()) return StreakInfo(0, 0, dailyStats.first().date)

        var current = 1
        var longest = 1
        var runningStreak = 1

        for (i in 1 until qualifyingDates.size) {
            val dayDiff = daysBetween(qualifyingDates[i], qualifyingDates[i - 1])
            if (dayDiff == 1) {
                runningStreak++
            } else {
                longest = maxOf(longest, runningStreak)
                runningStreak = 1
            }
        }
        longest = maxOf(longest, runningStreak)

        // Current streak only counts if the most recent qualifying day is
        // today or yesterday — otherwise it's broken, not just "at 1."
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
        val mostRecent = qualifyingDates.first()
        val gapFromToday = daysBetween(today, mostRecent)
        current = if (gapFromToday <= 1) {
            var streak = 1
            for (i in 1 until qualifyingDates.size) {
                if (daysBetween(qualifyingDates[i], qualifyingDates[i - 1]) == 1) streak++ else break
            }
            streak
        } else 0

        return StreakInfo(
            currentStreakDays = current,
            longestStreakDays = longest,
            lastActiveDate = dateFormat.format(mostRecent)
        )
    }

    private fun daysBetween(a: Date, b: Date): Int {
        val diff = b.time - a.time
        return (diff / (24 * 60 * 60 * 1000L)).toInt()
    }
}
