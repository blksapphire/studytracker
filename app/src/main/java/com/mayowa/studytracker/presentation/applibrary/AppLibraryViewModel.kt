package com.mayowa.studytracker.presentation.applibrary

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.data.tracking.AppCategory
import com.mayowa.studytracker.data.tracking.AppClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledAppUi(
    val packageName: String,
    val label: String,
    val category: AppCategory,
    val isDistracting: Boolean
)

@HiltViewModel
class AppLibraryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appClassifier: AppClassifier
) : ViewModel() {

    val apps: StateFlow<List<InstalledAppUi>> = combine(
        appClassifier.distractingPackages,
        appClassifier.categories
    ) { distracting, categories -> buildAppList(distracting, categories) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun buildAppList(
        distracting: Set<String>,
        categories: Map<String, AppCategory>
    ): List<InstalledAppUi> {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != context.packageName }
            .map { info ->
                val label = pm.getApplicationLabel(info).toString()
                InstalledAppUi(
                    packageName = info.packageName,
                    label = label,
                    category = categories[info.packageName] ?: appClassifier.suggestCategory(info.packageName, label),
                    isDistracting = info.packageName in distracting
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun toggleDistracting(packageName: String) {
        viewModelScope.launch {
            val current = appClassifier.distractingPackages.first()
            val updated = if (packageName in current) current - packageName else current + packageName
            appClassifier.setDistractingPackages(updated)
        }
    }

    fun setCategory(packageName: String, category: AppCategory) {
        viewModelScope.launch { appClassifier.setCategory(packageName, category) }
    }
}
