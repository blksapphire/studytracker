package com.mayowa.studytracker.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.mayowa.studytracker.data.tracking.TrackingService
import com.mayowa.studytracker.data.tracking.UsagePermissionHelper

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var hasUsageAccess by remember { mutableStateOf(UsagePermissionHelper.hasUsageAccess(context)) }
    var step by remember { mutableStateOf(if (hasUsageAccess) 1 else 0) }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.size(180.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-30).dp).blur(65.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .16f), CircleShape))
        Box(Modifier.size(150.dp).align(Alignment.BottomStart).offset(x = (-35).dp, y = 40.dp).blur(55.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha = .14f), CircleShape))

        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(19.dp))
                }
                Text("StudyTracker", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    StepDot(active = step == 0)
                    StepDot(active = step == 1)
                }

                when (step) {
                    0 -> PermissionStep(
                        icon = Icons.Filled.Visibility,
                        eyebrow = "ONE QUICK SETUP",
                        title = "Know where your study time goes.",
                        body = "StudyTracker watches which app is open so it can separate focused study from distractions. Your activity stays on your phone.",
                        button = "Give usage access",
                        onButton = { context.startActivity(UsagePermissionHelper.requestUsageAccessIntent()) },
                        secondary = "I already did this",
                        onSecondary = {
                            hasUsageAccess = UsagePermissionHelper.hasUsageAccess(context)
                            if (hasUsageAccess) step = 1
                        }
                    )
                    1 -> PermissionStep(
                        icon = Icons.Filled.BatteryChargingFull,
                        eyebrow = "ONE MORE THING",
                        title = "Let your streak keep running.",
                        body = "Some phones stop background apps to save battery. Allowing StudyTracker to keep running makes your study history much more reliable.",
                        button = "Allow background tracking",
                        onButton = { context.startActivity(UsagePermissionHelper.requestIgnoreBatteryOptimizationsIntent(context)) },
                        secondary = "Skip for now",
                        onSecondary = {
                            TrackingService.start(context)
                            onFinished()
                        }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Icon(Icons.Filled.Lock, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Your study history stays on your device.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StepDot(active: Boolean) {
    Box(Modifier.height(6.dp).width(if (active) 28.dp else 10.dp).clip(RoundedCornerShape(10.dp)).background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant))
}

@Composable
private fun PermissionStep(icon: androidx.compose.ui.graphics.vector.ImageVector, eyebrow: String, title: String, body: String, button: String, onButton: () -> Unit, secondary: String, onSecondary: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
        }
        Text(eyebrow, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onButton, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) { Text(button, fontWeight = FontWeight.Bold) }
        TextButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth()) { Text(secondary) }
    }
}
