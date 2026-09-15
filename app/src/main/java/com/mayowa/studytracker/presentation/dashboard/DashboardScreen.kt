package com.mayowa.studytracker.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    onOpenHistory: () -> Unit,
    onOpenAppLibrary: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

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

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(label = "Current streak", value = "🔥 ${state.currentStreakDays}d", accent = MaterialTheme.colorScheme.secondaryContainer, onAccent = MaterialTheme.colorScheme.onSecondaryContainer)
            StatCard(label = "Best streak", value = "⭐ ${state.longestStreakDays}d", accent = MaterialTheme.colorScheme.tertiaryContainer, onAccent = MaterialTheme.colorScheme.onTertiaryContainer)
        }

        Spacer(Modifier.height(32.dp))

        if (state.isSessionLive) {
            AssistChip(onClick = {}, label = { Text("Live session in progress") })
            Spacer(Modifier.height(16.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onOpenHistory) { Text("History") }
            OutlinedButton(onClick = onOpenAppLibrary) { Text("App Library") }
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
