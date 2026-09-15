package com.mayowa.studytracker.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayowa.studytracker.data.tracking.TrackingService
import com.mayowa.studytracker.presentation.theme.MintAccent
import com.mayowa.studytracker.presentation.theme.SkyAccent
import com.mayowa.studytracker.presentation.theme.TealPrimary
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(onOpenHistory: () -> Unit, onOpenAppLibrary: () -> Unit, viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val isPaused by TrackingService.isPaused.collectAsState()
    val liveElapsedMillis by TrackingService.liveElapsedMillis.collectAsState()
    val isFocusMode by TrackingService.isFocusMode.collectAsState()
    val focusRemainingMillis by TrackingService.focusRemainingMillis.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HeroStudyCard(state.todayStudyMillis, state.goalMillis, state.weeklyStudyMillis, state.isSessionLive, isFocusMode, liveElapsedMillis, focusRemainingMillis) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction(Modifier.weight(1f), Icons.Filled.Timer, if (isFocusMode) "Focus on" else "Focus", if (isFocusMode) formatFocusDuration(focusRemainingMillis) else "25 min") { TrackingService.startFocus(context) }
                QuickAction(Modifier.weight(1f), Icons.Filled.History, "History", "Review sessions", onOpenHistory)
            }
        }
        item { Text("Your week", style = MaterialTheme.typography.titleLarge) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(Modifier.weight(1f), Icons.Filled.Schedule, formatDuration(state.weeklyStudyMillis), "Study time")
                MetricCard(Modifier.weight(1f), Icons.Filled.LocalFireDepartment, "${state.currentStreakDays}d", "Current streak")
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard(Modifier.weight(1f), Icons.Filled.PlayCircle, state.weeklySessionCount.toString(), "Sessions")
                MetricCard(Modifier.weight(1f), Icons.Filled.EmojiEvents, "${state.longestStreakDays}d", "Best streak")
            }
        }
        item { SectionCard("Keep your momentum", "Small focused sessions add up. Keep your streak alive.", "Progress", Icons.Filled.TrendingUp) { } }
        item { SectionCard("Study apps", "Choose which apps should count as productive for you.", "Manage apps", Icons.Filled.Apps, onOpenAppLibrary) }
        item { SessionControls(state.isSessionLive, isPaused, { TrackingService.start(context) }, { TrackingService.pause(context) }, { TrackingService.resume(context) }, { TrackingService.stop(context) }) }
    }
}

@Composable
private fun HeroStudyCard(todayMillis: Long, goalMillis: Long, weeklyMillis: Long, isLive: Boolean, isFocus: Boolean, liveMillis: Long, focusRemaining: Long) {
    val progress = if (goalMillis > 0) (todayMillis.toFloat() / goalMillis).coerceIn(0f, 1f) else 0f
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(TealPrimary, Color(0xFF817BFF), SkyAccent)), RoundedCornerShape(30.dp)).padding(22.dp)) {
            Box(modifier = Modifier.size(130.dp).offset(x = 205.dp, y = (-35).dp).blur(30.dp).background(MintAccent.copy(alpha = 0.35f), CircleShape))
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("TODAY", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.75f))
                        Text(if (isLive) if (isFocus) "You're in the zone" else "You're studying" else "Ready when you are", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    }
                    Surface(color = Color.White.copy(alpha = 0.16f), shape = CircleShape) {
                        Icon(if (isFocus) Icons.Filled.Bolt else Icons.Filled.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.padding(11.dp).size(22.dp))
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(if (isFocus) formatFocusDuration(focusRemaining) else formatDuration(if (isLive) liveMillis else todayMillis), style = MaterialTheme.typography.displayMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(if (isFocus) "Focus remaining" else "studied today", color = Color.White.copy(alpha = 0.76f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                if (goalMillis > 0) {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = Color.White, trackColor = Color.White.copy(alpha = 0.22f))
                    Spacer(Modifier.height(7.dp))
                    Text("${(progress * 100).roundToInt()}% of today's goal", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelMedium)
                } else Text("${formatDuration(weeklyMillis)} this week", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun QuickAction(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ElevatedCard(modifier = modifier, onClick = onClick, shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) { Icon(icon, null, tint = TealPrimary, modifier = Modifier.padding(9.dp).size(20.dp)) }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MetricCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(22.dp)) {
        Row(modifier = Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape) { Icon(icon, null, modifier = Modifier.padding(9.dp).size(19.dp), tint = TealPrimary) }
            Spacer(Modifier.width(11.dp))
            Column { Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun SectionCard(title: String, subtitle: String, action: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(24.dp), onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) { Icon(icon, null, tint = TealPrimary, modifier = Modifier.padding(10.dp).size(21.dp)) }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(2.dp)); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Filled.ChevronRight, contentDescription = action)
        }
    }
}

@Composable
private fun SessionControls(isLive: Boolean, isPaused: Boolean, onStart: () -> Unit, onPause: () -> Unit, onResume: () -> Unit, onStop: () -> Unit) {
    if (!isLive) {
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) { Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Start a study session") }
        return
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = if (isPaused) onResume else onPause, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(17.dp)) { Icon(if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, null); Spacer(Modifier.width(6.dp)); Text(if (isPaused) "Resume" else "Pause") }
        OutlinedButton(onClick = onStop, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(17.dp)) { Icon(Icons.Filled.Stop, null); Spacer(Modifier.width(6.dp)); Text("Finish") }
    }
}

fun formatDuration(millis: Long): String {
    val totalMinutes = millis / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

private fun formatFocusDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0L)
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
