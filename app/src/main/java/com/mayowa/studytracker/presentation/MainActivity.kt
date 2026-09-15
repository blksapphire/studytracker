package com.mayowa.studytracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.mayowa.studytracker.data.profile.ProfileStore
import com.mayowa.studytracker.data.tracking.TrackingService
import com.mayowa.studytracker.data.tracking.UsagePermissionHelper
import com.mayowa.studytracker.presentation.applibrary.AppLibraryScreen
import com.mayowa.studytracker.presentation.dashboard.DashboardScreen
import com.mayowa.studytracker.presentation.goals.GoalsScreen
import com.mayowa.studytracker.presentation.history.HistoryScreen
import com.mayowa.studytracker.presentation.insights.InsightsScreen
import com.mayowa.studytracker.presentation.onboarding.OnboardingScreen
import com.mayowa.studytracker.presentation.planner.StudyPlanScreen
import com.mayowa.studytracker.presentation.profile.ProfileScreen
import com.mayowa.studytracker.presentation.session.SessionScreen
import com.mayowa.studytracker.presentation.settings.SettingsScreen
import com.mayowa.studytracker.presentation.theme.MintAccent
import com.mayowa.studytracker.presentation.theme.StudyTrackerTheme
import com.mayowa.studytracker.presentation.theme.TealPrimary
import dagger.hilt.android.AndroidEntryPoint

private data class BottomDest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val BOTTOM_DESTINATIONS = listOf(
    BottomDest("dashboard", "Home", Icons.Filled.Home),
    BottomDest("session", "Focus", Icons.Filled.Timer),
    BottomDest("planner", "Plan", Icons.Filled.AutoAwesome),
    BottomDest("insights", "Progress", Icons.Filled.TrendingUp),
    BottomDest("goals", "Goals", Icons.Filled.Star)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val profileStore = remember { ProfileStore(context) }
                    val profile by profileStore.profile.collectAsState(initial = com.mayowa.studytracker.data.profile.StudentProfile())
                    var hasUsageAccess by remember { mutableStateOf(UsagePermissionHelper.hasUsageAccess(context)) }
                    val ready = hasUsageAccess && profile.completed
                    if (!ready) {
                        OnboardingScreen(onFinished = {
                            hasUsageAccess = UsagePermissionHelper.hasUsageAccess(context)
                            if (hasUsageAccess) TrackingService.start(context)
                        })
                    } else {
                        StudentScaffold(profile.name)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentScaffold(name: String) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val title = when (currentRoute) {
        "dashboard" -> if (name.isBlank()) "Good to see you" else "Hey, ${name.split(" ").first()} 👋"
        "session" -> "Focus time"
        "planner" -> "Today's plan"
        "history" -> "Study history"
        "insights" -> "Your progress"
        "goals" -> "Your goals"
        "profile" -> "Your profile"
        "app_library" -> "Study apps"
        "settings" -> "Settings"
        else -> "StudyTracker"
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(Modifier.fillMaxWidth().height(92.dp).background(MaterialTheme.colorScheme.background)) {
                Box(Modifier.size(150.dp).offset(x = 250.dp, y = (-65).dp).blur(35.dp).background(MintAccent.copy(alpha = 0.22f), RoundedCornerShape(100.dp)))
                Row(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(title, style = MaterialTheme.typography.headlineSmall)
                        if (currentRoute == "dashboard") Text("Make today count.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { navController.navigate("profile") }) { Icon(Icons.Filled.AccountCircle, contentDescription = "Profile") }
                }
            }
        },
        bottomBar = { StudentBottomBar(navController, currentRoute) }
    ) { padding ->
        NavHost(navController = navController, startDestination = "dashboard", modifier = Modifier.padding(padding), enterTransition = { fadeIn() }, exitTransition = { fadeOut() }) {
            composable("dashboard") { DashboardScreen(onOpenHistory = { navController.navigate("history") }, onOpenAppLibrary = { navController.navigate("app_library") }) }
            composable("session") { SessionScreen() }
            composable("planner") { StudyPlanScreen() }
            composable("history") { HistoryScreen() }
            composable("insights") { InsightsScreen() }
            composable("goals") { GoalsScreen() }
            composable("profile") { ProfileScreen() }
            composable("app_library") { AppLibraryScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
private fun StudentBottomBar(navController: androidx.navigation.NavHostController, currentRoute: String?) {
    Surface(tonalElevation = 0.dp, shadowElevation = 12.dp, color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f)) {
        NavigationBar(modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp), containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f), tonalElevation = 0.dp) {
            BOTTOM_DESTINATIONS.forEach { dest ->
                NavigationBarItem(
                    selected = currentRoute == dest.route || navController.currentDestination?.hierarchy?.any { it.route == dest.route } == true,
                    onClick = { navController.navigate(dest.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(dest.icon, contentDescription = dest.label) }, label = { Text(dest.label) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, selectedTextColor = TealPrimary, indicatorColor = TealPrimary, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}
