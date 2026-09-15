package com.mayowa.studytracker.presentation.applibrary

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayowa.studytracker.data.tracking.AppClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstalledAppUi(
    val packageName: String,
    val label: String,
    val isDistracting: Boolean
)

@HiltViewModel
class AppLibraryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appClassifier: AppClassifier
) : ViewModel() {

    val apps: StateFlow<List<InstalledAppUi>> = appClassifier.distractingPackages
        .map { distracting -> buildAppList(distracting) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun buildAppList(distracting: Set<String>): List<InstalledAppUi> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // skip system apps — user rarely needs to classify these
            .map { appInfo ->
                InstalledAppUi(
                    packageName = appInfo.packageName,
                    label = pm.getApplicationLabel(appInfo).toString(),
                    isDistracting = appInfo.packageName in distracting
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
}
