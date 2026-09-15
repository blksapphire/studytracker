package com.mayowa.studytracker.data.profile

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studentProfileDataStore by preferencesDataStore(name = "student_profile")

data class StudentProfile(
    val name: String = "",
    val subjects: List<String> = emptyList(),
    val goal: String = "Build a consistent study routine",
    val examDateMillis: Long? = null,
    val dailyTargetMinutes: Int = 60,
    val nudgeEnabled: Boolean = true,
    val completed: Boolean = false
)

class ProfileStore(private val context: Context) {
    private object Keys {
        val name = stringPreferencesKey("name")
        val subjects = stringPreferencesKey("subjects")
        val goal = stringPreferencesKey("goal")
        val examDate = longPreferencesKey("exam_date")
        val dailyTarget = longPreferencesKey("daily_target")
        val nudge = booleanPreferencesKey("nudge_enabled")
        val completed = booleanPreferencesKey("completed")
    }

    val profile: Flow<StudentProfile> = context.studentProfileDataStore.data.map { p ->
        StudentProfile(
            name = p[Keys.name].orEmpty(),
            subjects = p[Keys.subjects].orEmpty().split('|').map(String::trim).filter(String::isNotEmpty),
            goal = p[Keys.goal] ?: "Build a consistent study routine",
            examDateMillis = p[Keys.examDate],
            dailyTargetMinutes = (p[Keys.dailyTarget] ?: 60L).toInt().coerceIn(15, 480),
            nudgeEnabled = p[Keys.nudge] ?: true,
            completed = p[Keys.completed] ?: false
        )
    }

    suspend fun save(
        name: String,
        subjects: List<String>,
        goal: String,
        examDateMillis: Long?,
        dailyTargetMinutes: Int,
        nudgeEnabled: Boolean
    ) {
        context.studentProfileDataStore.edit { p ->
            p[Keys.name] = name.trim()
            p[Keys.subjects] = subjects.joinToString("|")
            p[Keys.goal] = goal.trim()
            if (examDateMillis != null) p[Keys.examDate] = examDateMillis else p.remove(Keys.examDate)
            p[Keys.dailyTarget] = dailyTargetMinutes.coerceIn(15, 480).toLong()
            p[Keys.nudge] = nudgeEnabled
            p[Keys.completed] = true
        }
    }

    suspend fun setNudgeEnabled(enabled: Boolean) {
        context.studentProfileDataStore.edit { it[Keys.nudge] = enabled }
    }
}
