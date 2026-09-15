package com.mayowa.studytracker.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayowa.studytracker.domain.model.StudySession
import com.mayowa.studytracker.presentation.dashboard.formatDuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val sessionDateFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
private val sessionTimeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val dailyStats by viewModel.dailyStats.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()

    val totalMillis = sessions.sumOf { it.durationMillis }
    val longestMillis = sessions.maxOfOrNull { it.durationMillis } ?: 0L
    val completed = sessions.count { it.endedAt != null }

    if (isEmpty) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            EmptyHistoryCard()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                Box(
                    Modifier.size(110.dp).align(Alignment.TopEnd).blur(38.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .18f), CircleShape)
                )
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text("Your study history", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text("See how your effort adds up over time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                HistoryMetric("Study time", formatDuration(totalMillis), Icons.Filled.AccessTime, Modifier.weight(1f))
                HistoryMetric("Sessions", sessions.size.toString(), Icons.Filled.CheckCircle, Modifier.weight(1f))
                HistoryMetric("Longest", formatDuration(longestMillis), Icons.Filled.LocalFireDepartment, Modifier.weight(1f))
            }
        }

        if (sessions.isNotEmpty()) {
            item {
                Text("Recent sessions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 8.dp))
            }
            items(sessions, key = { it.id }) { session -> SessionHistoryCard(session) }
        }

        if (dailyStats.isNotEmpty()) {
            item {
                Text("Your days", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 10.dp))
            }
            items(dailyStats, key = { it.date }) { day -> DailyHistoryCard(day.date, day.totalStudyMillis, day.sessionCount, day.longestSessionMillis, day.topTag) }
        }
    }
}

@Composable
private fun HistoryMetric(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .62f)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun SessionHistoryCard(session: StudySession) {
    ElevatedCard(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = .11f)), contentAlignment = Alignment.Center) {
                        Icon(if (session.endedAt == null) Icons.Filled.AccessTime else Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
                    }
                    Column {
                        Text(session.tag?.takeIf { it.isNotBlank() } ?: "Study session", fontWeight = FontWeight.Bold)
                        Text(
                            if (session.endedAt == null) "Started ${sessionTimeFormatter.format(Date(session.startedAt))} · Live" else "${sessionDateFormatter.format(Date(session.startedAt))} · ${sessionTimeFormatter.format(Date(session.startedAt))}",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(formatDuration(session.durationMillis), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (session.appTrail.isNotEmpty()) {
                    AssistChip(onClick = {}, leadingIcon = { Icon(Icons.Filled.Apps, null, Modifier.size(16.dp)) }, label = { Text("${session.appTrail.distinct().size} app${if (session.appTrail.distinct().size == 1) "" else "s"}") })
                }
                if (session.distractionCount > 0) {
                    AssistChip(onClick = {}, label = { Text("${session.distractionCount} distraction${if (session.distractionCount == 1) "" else "s"}") })
                } else {
                    AssistChip(onClick = {}, label = { Text("Focused") })
                }
            }
        }
    }
}

@Composable
private fun DailyHistoryCard(date: String, total: Long, sessions: Int, longest: Long, topTag: String?) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(date, fontWeight = FontWeight.Bold)
                Text("$sessions session${if (sessions == 1) "" else "s"}${topTag?.let { " · $it" } ?: ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatDuration(total), fontWeight = FontWeight.ExtraBold)
                Text("Best ${formatDuration(longest)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    ElevatedCard(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AccessTime, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            }
            Text("Your study story starts here", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text("Complete your first focus session and your progress will appear here.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
