package com.mayowa.studytracker.data.tracking

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppCategory(val label: String) {
    STUDY("Study"),
    PRODUCTIVITY("Productivity"),
    SOCIAL("Social"),
    ENTERTAINMENT("Entertainment"),
    GAMES("Games"),
    OTHER("Other")
}

/** User-editable app classification used by tracking and Focus Mode. */
@Singleton
class AppClassifier @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val DISTRACTING_PACKAGES = stringSetPreferencesKey("distracting_packages")
        val APP_CATEGORIES = stringSetPreferencesKey("app_categories")

        val DEFAULT_DISTRACTING = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.twitter.android",
            "com.facebook.katana",
            "com.snapchat.android",
            "com.google.android.youtube",
            "com.reddit.frontpage",
            "com.whatsapp"
        )
    }

    val distractingPackages: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[DISTRACTING_PACKAGES] ?: DEFAULT_DISTRACTING
    }

    val categories: Flow<Map<String, AppCategory>> = dataStore.data.map { prefs ->
        prefs[APP_CATEGORIES].orEmpty().mapNotNull { entry ->
            val parts = entry.split('|', limit = 2)
            if (parts.size != 2) return@mapNotNull null
            val category = runCatching { AppCategory.valueOf(parts[1]) }.getOrNull() ?: return@mapNotNull null
            parts[0] to category
        }.toMap()
    }

    suspend fun setDistractingPackages(packages: Set<String>) {
        dataStore.edit { it[DISTRACTING_PACKAGES] = packages }
    }

    suspend fun setCategory(packageName: String, category: AppCategory) {
        dataStore.edit { prefs ->
            val current = prefs[APP_CATEGORIES].orEmpty()
            prefs[APP_CATEGORIES] = current.filterNot { it.startsWith("$packageName|") }.toSet() + "$packageName|${category.name}"
        }
    }

    fun suggestCategory(packageName: String, label: String): AppCategory {
        val value = "$packageName $label".lowercase()
        return when {
            listOf("youtube", "netflix", "spotify", "twitch", "primevideo", "video").any(value::contains) -> AppCategory.ENTERTAINMENT
            listOf("instagram", "facebook", "twitter", "x.com", "tiktok", "snapchat", "reddit", "telegram", "whatsapp", "discord").any(value::contains) -> AppCategory.SOCIAL
            listOf("game", "games", "mobile legends", "pubg", "free fire", "roblox", "minecraft").any(value::contains) -> AppCategory.GAMES
            listOf("google docs", "drive", "notion", "evernote", "office", "word", "excel", "calendar", "todo", "trello", "slack").any(value::contains) -> AppCategory.PRODUCTIVITY
            listOf("coursera", "udemy", "duolingo", "quizlet", "khan", "school", "learn", "study", "anki").any(value::contains) -> AppCategory.STUDY
            else -> AppCategory.OTHER
        }
    }

    suspend fun isDistracting(packageName: String): Boolean = packageName in distractingPackages.first()
}
