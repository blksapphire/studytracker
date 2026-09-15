package com.mayowa.studytracker.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.mayowa.studytracker.data.tracking.UsagePermissionHelper

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var hasUsageAccess by remember { mutableStateOf(UsagePermissionHelper.hasUsageAccess(context)) }
    var nudgesEnabled by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box(Modifier.fillMaxWidth().height(100.dp)) {
            Box(Modifier.size(100.dp).align(Alignment.TopEnd).blur(38.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .15f), CircleShape))
            Column(Modifier.padding(top = 8.dp)) {
                Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text("Keep StudyTracker working the way you like.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingCard(
                icon = Icons.Filled.Security,
                title = "Usage access",
                description = if (hasUsageAccess) "Connected · tracking can see your active app" else "Needed to tell study time from distraction time",
                action = if (hasUsageAccess) "Granted" else "Fix",
                enabled = !hasUsageAccess,
                onAction = {
                    context.startActivity(UsagePermissionHelper.requestUsageAccessIntent())
                    hasUsageAccess = UsagePermissionHelper.hasUsageAccess(context)
                }
            )

            SettingCard(
                icon = Icons.Filled.BatteryChargingFull,
                title = "Battery protection",
                description = "Allow background tracking to stay reliable on your phone.",
                action = "Open",
                enabled = true,
                onAction = { context.startActivity(UsagePermissionHelper.requestIgnoreBatteryOptimizationsIntent(context)) }
            )

            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.tertiary.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.NotificationsActive, null, tint = MaterialTheme.colorScheme.tertiary)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Focus nudges", fontWeight = FontWeight.Bold)
                        Text("A gentle reminder when you drift into a distracting app.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = nudgesEnabled, onCheckedChange = { nudgesEnabled = it })
                }
            }

            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Icon(Icons.Filled.Settings, null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Private by default", fontWeight = FontWeight.Bold)
                        Text("Your study history stays on this device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String, action: String, enabled: Boolean, onAction: () -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onAction, enabled = enabled) { Text(action) }
        }
    }
}
