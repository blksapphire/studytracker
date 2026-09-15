package com.mayowa.studytracker.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayowa.studytracker.presentation.dashboard.formatDuration
import com.mayowa.studytracker.presentation.theme.SkyAccent
import com.mayowa.studytracker.presentation.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val firstName = state.name.ifBlank { "Student" }.split(" ").first()
    val examDays = state.examDateMillis?.let { ((it - System.currentTimeMillis()) / 86_400_000L).toInt() }
    val levelStart = (state.level - 1) * 250
    val levelProgress = ((state.xp - levelStart).coerceAtLeast(0) / 250f).coerceIn(0f, 1f)
    val xpIntoLevel = (state.xp - levelStart).coerceIn(0, 249)
    val xpToNext = 250 - xpIntoLevel

    LazyColumn(contentPadding = PaddingValues(18.dp, 4.dp, 18.dp, 30.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(TealPrimary, Color(0xFF817BFF), SkyAccent)), RoundedCornerShape(30.dp)).padding(22.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color.White.copy(alpha = .18f), modifier = Modifier.size(64.dp)) { Box(contentAlignment = Alignment.Center) { Text(firstName.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) } }
                            Spacer(Modifier.width(14.dp))
                            Column { Text("YOUR STUDY PROFILE", color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.labelMedium); Text("$firstName's journey", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
                        }
                        Text(state.goal, color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { Pill("Level ${state.level}", Icons.Filled.Star); Pill("${state.xp} XP", Icons.Filled.AutoAwesome) }
                    }
                }
            }
        }
        item {
            ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Level ${state.level}", fontWeight = FontWeight.Bold); Text("$xpToNext XP to next", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    LinearProgressIndicator(progress = { levelProgress }, modifier = Modifier.fillMaxWidth().height(8.dp), strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                    Text("Keep showing up. Every focused minute moves you forward.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Text("Your numbers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) { StatCard(Modifier.weight(1f), Icons.Filled.Schedule, formatDuration(state.totalStudyMillis), "Study time"); StatCard(Modifier.weight(1f), Icons.Filled.LocalFireDepartment, "${state.streakDays}d", "Current streak") } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) { StatCard(Modifier.weight(1f), Icons.Filled.MenuBook, state.sessions.toString(), "Sessions"); StatCard(Modifier.weight(1f), Icons.Filled.LocalFireDepartment, "${state.bestStreakDays}d", "Best streak") } }
        item {
            ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Exam countdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (state.examDateMillis == null) Text("No exam date yet. Add one in Goals to make the countdown work for you.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else { Text(if (examDays == null || examDays < 0) "Exam date has passed" else "$examDays days to go", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = TealPrimary); Text(SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date(state.examDateMillis)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        if (state.subjects.isNotEmpty()) {
            item { Text("Your subjects", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { state.subjects.take(4).forEach { SuggestionChip(onClick = {}, label = { Text(it) }) } } }
        }
    }
}

@Composable
private fun Pill(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Surface(color = Color.White.copy(alpha = .15f), shape = RoundedCornerShape(50.dp)) { Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(5.dp)); Text(text, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) } } }

@Composable
private fun StatCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) { ElevatedCard(modifier = modifier, shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Icon(icon, null, tint = TealPrimary, modifier = Modifier.size(21.dp)); Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
