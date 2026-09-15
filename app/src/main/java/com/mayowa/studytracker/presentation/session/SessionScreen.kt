package com.mayowa.studytracker.presentation.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import com.mayowa.studytracker.presentation.dashboard.formatDuration
import com.mayowa.studytracker.presentation.theme.MintAccent
import com.mayowa.studytracker.presentation.theme.TealPrimary

private val SUGGESTED_TAGS = listOf("General", "Reading", "Coursework", "Exam prep", "Coding")

@Composable
fun SessionScreen(viewModel: SessionViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val isPaused by TrackingService.isPaused.collectAsState()
    val isFocusMode by TrackingService.isFocusMode.collectAsState()
    val focusRemaining by TrackingService.focusRemainingMillis.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(10.dp))
        if (!state.isLive) {
            EmptyFocusState { TrackingService.startFocus(context) }
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(32.dp))) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(TealPrimary, Color(0xFF817BFF)))))
                Box(modifier = Modifier.size(150.dp).offset(x = 190.dp, y = (-30).dp).blur(35.dp).background(MintAccent.copy(alpha = 0.32f), CircleShape))
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(color = Color.White.copy(alpha = 0.15f), shape = CircleShape) {
                        Icon(if (isFocusMode) Icons.Filled.Bolt else Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.padding(13.dp).size(24.dp))
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(if (isFocusMode) "FOCUS MODE" else "STUDY SESSION", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.72f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (isFocusMode) formatFocus(focusRemaining) else formatDuration(state.elapsedMillis),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(if (isFocusMode) "Stay with it — you've got this." else "You're making progress.", color = Color.White.copy(alpha = 0.78f))
                    Spacer(Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilledTonalButton(onClick = { if (isPaused) TrackingService.resume(context) else TrackingService.pause(context) }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White.copy(alpha = 0.18f), contentColor = Color.White)) {
                            Icon(if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (isPaused) "Resume" else "Pause")
                        }
                        if (isFocusMode) {
                            OutlinedButton(onClick = { TrackingService.stopFocus(context) }, border = ButtonDefaults.outlinedButtonBorder(enabled = true), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("End focus") }
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Text("What are you working on?", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                items(SUGGESTED_TAGS) { tag ->
                    FilterChip(selected = state.currentTag == tag, onClick = { viewModel.tagCurrentSession(tag) }, label = { Text(tag) })
                }
            }
            Spacer(Modifier.height(20.dp))
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Keep going", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("StudyTracker is quietly tracking your productive time.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFocusState(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape) {
                Icon(Icons.Filled.Bolt, null, tint = TealPrimary, modifier = Modifier.padding(20.dp).size(42.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("Ready to focus?", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Start a 25-minute focus session and let StudyTracker handle the tracking.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(22.dp))
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                Icon(Icons.Filled.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text("Start Focus")
            }
        }
    }
}

private fun formatFocus(millis: Long): String {
    val seconds = (millis / 1000).coerceAtLeast(0L)
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}
