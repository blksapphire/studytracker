package com.mayowa.studytracker.data.tracking

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Classifies foreground packages as "distracting" or not. Ships with a
 * curated default list; user can edit it from the App Library screen.
 */
@Singleton
class AppClassifier @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val DISTRACTING_PACKAGES = stringSetPreferencesKey("distracting_packages")

        // Seed list — ships in v1, editable from App Library screen.
        val DEFAULT_DISTRACTING = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically", // TikTok
            "com.twitter.android",
            "com.facebook.katana",
            "com.snapchat.android",
            "com.google.android.youtube",
            "com.reddit.frontpage",
            "com.whatsapp" // debatable — surface this as user-editable, not hardcoded assumption
        )
    }

    val distractingPackages: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[DISTRACTING_PACKAGES] ?: DEFAULT_DISTRACTING
    }

    suspend fun setDistractingPackages(packages: Set<String>) {
        dataStore.edit { it[DISTRACTING_PACKAGES] = packages }
    }

    suspend fun isDistracting(packageName: String): Boolean {
        return packageName in distractingPackages.first()
    }
}
