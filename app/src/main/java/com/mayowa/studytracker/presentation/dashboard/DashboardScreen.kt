package com.mayowa.studytracker.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayowa.studytracker.data.tracking.TrackingService
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    onOpenHistory: () -> Unit,
    onOpenAppLibrary: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isPaused by TrackingService.isPaused.collectAsState()
    val liveElapsedMillis by TrackingService.liveElapsedMillis.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Today", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            formatDuration(state.todayStudyMillis),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )

        if (state.goalMillis > 0) {
            Spacer(Modifier.height(4.dp))
            val progress = (state.todayStudyMillis.toFloat() / state.goalMillis).coerceIn(0f, 1f)
            Text(
                "${(progress * 100).roundToInt()}% of your ${formatDuration(state.goalMillis)} goal",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(0.8f).height(8.dp).clip(CircleShape)
            )
        }

        Spacer(Modifier.height(28.dp))

        if (state.isSessionLive) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPaused) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (isPaused) "SESSION PAUSED" else "● LIVE SESSION",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatLiveDuration(liveElapsedMillis),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        SessionControls(
            isLive = state.isSessionLive,
            isPaused = isPaused,
            onStart = { TrackingService.start(context) },
            onPause = { TrackingService.pause(context) },
            onResume = { TrackingService.resume(context) },
            onStop = { TrackingService.stop(context) }
        )

        Spacer(Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(label = "Current streak", value = "🔥 ${state.currentStreakDays}d", accent = MaterialTheme.colorScheme.secondaryContainer, onAccent = MaterialTheme.colorScheme.onSecondaryContainer)
            StatCard(label = "Best streak", value = "⭐ ${state.longestStreakDays}d", accent = MaterialTheme.colorScheme.tertiaryContainer, onAccent = MaterialTheme.colorScheme.onTertiaryContainer)
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onOpenHistory) { Text("History") }
            OutlinedButton(onClick = onOpenAppLibrary) { Text("App Library") }
        }
    }
}

@Composable
private fun SessionControls(
    isLive: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    if (!isLive) {
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Start Session")
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = if (isPaused) onResume else onPause,
            modifier = Modifier.weight(1f).height(52.dp)
        ) {
            Icon(
                if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                contentDescription = null
            )
            Spacer(Modifier.width(6.dp))
            Text(if (isPaused) "Resume" else "Pause")
        }

        OutlinedButton(
            onClick = onStop,
            modifier = Modifier.weight(1f).height(52.dp)
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Stop")
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, accent: androidx.compose.ui.graphics.Color, onAccent: androidx.compose.ui.graphics.Color) {
    Card(colors = CardDefaults.cardColors(containerColor = accent)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = onAccent)
            Text(label, style = MaterialTheme.typography.labelMedium, color = onAccent)
        }
    }
}

fun formatDuration(millis: Long): String {
    val totalMinutes = millis / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

private fun formatLiveDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
