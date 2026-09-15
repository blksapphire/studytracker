package com.mayowa.studytracker.domain.usecase

import com.mayowa.studytracker.domain.model.DailyStats

data class StudyPlanItem(val subject: String, val minutes: Int, val reason: String)
data class StudyPlan(val totalMinutes: Int, val items: List<StudyPlanItem>, val message: String)

class BuildStudyPlanUseCase {
    operator fun invoke(subjects: List<String>, dailyTargetMinutes: Int, stats: List<DailyStats>): StudyPlan {
        val safeSubjects = subjects.filter { it.isNotBlank() }.distinct().ifEmpty { listOf("Study") }
        val recentBySubject = stats.mapNotNull { it.topTag?.let { tag -> tag to it.totalStudyMillis } }
            .groupBy({ it.first }, { it.second }).mapValues { it.value.sum() }
        val ordered = safeSubjects.sortedBy { recentBySubject[it] ?: 0L }
        val target = dailyTargetMinutes.coerceIn(15, 480)
        val base = target / ordered.size
        var remainder = target - base * ordered.size
        val items = ordered.map { subject ->
            val extra = if (remainder-- > 0) 1 else 0
            StudyPlanItem(subject, (base + extra).coerceAtLeast(5), if ((recentBySubject[subject] ?: 0L) == 0L) "Needs attention" else "Keep the momentum")
        }
        val message = when {
            stats.isEmpty() -> "Start with one focused block. Your plan gets smarter as you study."
            ordered.firstOrNull()?.let { recentBySubject[it] ?: 0L } == 0L -> "Let's give an under-studied subject some attention today."
            else -> "A balanced plan based on your recent study pattern."
        }
        return StudyPlan(target, items, message)
    }
}
