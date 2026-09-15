package com.mayowa.studytracker.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mayowa.studytracker.data.tracking.UsagePermissionHelper
import com.mayowa.studytracker.presentation.applibrary.AppLibraryScreen
import com.mayowa.studytracker.presentation.dashboard.DashboardScreen
import com.mayowa.studytracker.presentation.goals.GoalsScreen
import com.mayowa.studytracker.presentation.history.HistoryScreen
import com.mayowa.studytracker.presentation.insights.InsightsScreen
import com.mayowa.studytracker.presentation.onboarding.OnboardingScreen
import com.mayowa.studytracker.presentation.session.SessionScreen
import com.mayowa.studytracker.presentation.settings.SettingsScreen
import com.mayowa.studytracker.presentation.theme.StudyTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

private data class BottomDest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val BOTTOM_DESTINATIONS = listOf(
    BottomDest("dashboard", "Home", Icons.Filled.Home),
    BottomDest("session", "Session", Icons.Filled.PlayArrow),
    BottomDest("history", "History", Icons.Filled.DateRange),
    BottomDest("insights", "Insights", Icons.Filled.Info),
    BottomDest("goals", "Goals", Icons.Filled.Star)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    var onboarded by remember { mutableStateOf(UsagePermissionHelper.hasUsageAccess(context)) }

                    if (!onboarded) {
                        OnboardingScreen(onFinished = { onboarded = true })
                    } else {
                        AppScaffold()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold() {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StudyTracker") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination

                BOTTOM_DESTINATIONS.forEach { dest ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    onOpenHistory = { navController.navigate("history") },
                    onOpenAppLibrary = { navController.navigate("app_library") }
                )
            }
            composable("session") { SessionScreen() }
            composable("history") { HistoryScreen() }
            composable("insights") { InsightsScreen() }
            composable("goals") { GoalsScreen() }
            composable("app_library") { AppLibraryScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
