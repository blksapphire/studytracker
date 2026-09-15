package com.mayowa.studytracker.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mayowa.studytracker.data.tracking.UsagePermissionHelper

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var hasUsageAccess by remember { mutableStateOf(UsagePermissionHelper.hasUsageAccess(context)) }
    var nudgesEnabled by remember { mutableStateOf(true) } // TODO: back with DataStore once nudge worker lands

    Column(modifier = Modifier.fillMaxSize()) {
        ListItem(
            headlineContent = { Text("Usage access") },
            supportingContent = { Text(if (hasUsageAccess) "Granted" else "Not granted — tracking won't work") },
            trailingContent = {
                if (!hasUsageAccess) {
                    TextButton(onClick = {
                        context.startActivity(UsagePermissionHelper.requestUsageAccessIntent())
                    }) { Text("Fix") }
                }
            }
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Battery optimization exemption") },
            supportingContent = { Text("Recommended — prevents some phones from killing tracking") },
            trailingContent = {
                TextButton(onClick = {
                    context.startActivity(UsagePermissionHelper.requestIgnoreBatteryOptimizationsIntent(context))
                }) { Text("Open") }
            }
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Lock-screen nudges") },
            supportingContent = { Text("Gentle reminder when you open a distracting app right after studying") },
            trailingContent = {
                Switch(checked = nudgesEnabled, onCheckedChange = { nudgesEnabled = it })
            }
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Export my data") },
            supportingContent = { Text("Not yet available — all data stays local for now") }
        )
    }
}
