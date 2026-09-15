package com.mayowa.studytracker.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mayowa.studytracker.data.tracking.TrackingService
import com.mayowa.studytracker.data.tracking.UsagePermissionHelper

/**
 * Two-step ask, deliberately not bundled into one dialog:
 * 1) Usage access (required — app can't function without it)
 * 2) Battery optimization exemption (strongly recommended — several OEMs
 *    kill the foreground service without it)
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var hasUsageAccess by remember { mutableStateOf(UsagePermissionHelper.hasUsageAccess(context)) }
    var step by remember { mutableStateOf(if (hasUsageAccess) 1 else 0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (step) {
            0 -> {
                Text("Grant usage access", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(
                    "StudyTracker needs Usage Access to see which app is in the foreground — that's how it tells study time apart from distraction time. Nothing leaves your device.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { context.startActivity(UsagePermissionHelper.requestUsageAccessIntent()) }) {
                    Text("Open Settings")
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    hasUsageAccess = UsagePermissionHelper.hasUsageAccess(context)
                    if (hasUsageAccess) step = 1
                }) {
                    Text("I've granted it — continue")
                }
            }
            1 -> {
                Text("Keep tracking running", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Some phones aggressively kill background apps to save battery. Exempting StudyTracker keeps your streak accurate — it uses negligible battery either way.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    context.startActivity(UsagePermissionHelper.requestIgnoreBatteryOptimizationsIntent(context))
                }) {
                    Text("Open Settings")
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    TrackingService.start(context)
                    onFinished()
                }) {
                    Text("Skip for now — start tracking")
                }
            }
        }
    }
}
