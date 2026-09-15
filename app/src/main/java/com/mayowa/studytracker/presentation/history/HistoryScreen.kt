package com.mayowa.studytracker.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    if (isEmpty) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No history yet — your first session will show up here.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (sessions.isNotEmpty()) {
            item {
                Text(
                    "Recent sessions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            items(sessions, key = { it.id }) { session ->
                SessionHistoryCard(session)
            }
        }

        if (dailyStats.isNotEmpty()) {
            item {
                Text(
                    "Daily breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)
                )
            }

            items(dailyStats, key = { it.date }) { day ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(day.date, fontWeight = FontWeight.Bold)
                            Text(
                                "${day.sessionCount} session${if (day.sessionCount == 1) "" else "s"}" +
                                    (day.topTag?.let { " · $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatDuration(day.totalStudyMillis), fontWeight = FontWeight.Bold)
                            Text(
                                "Longest ${formatDuration(day.longestSessionMillis)}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(session: StudySession) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        sessionDateFormatter.format(Date(session.startedAt)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (session.endedAt == null) {
                            "Started ${sessionTimeFormatter.format(Date(session.startedAt))} · Live"
                        } else {
                            "${sessionTimeFormatter.format(Date(session.startedAt))} – ${sessionTimeFormatter.format(Date(session.endedAt))}"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    formatDuration(session.durationMillis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!session.tag.isNullOrBlank() || session.appTrail.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    session.tag?.takeIf { it.isNotBlank() }?.let {
                        AssistChip(onClick = {}, label = { Text(it) })
                    }
                    if (session.appTrail.isNotEmpty()) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    "${session.appTrail.distinct().size} app" +
                                        if (session.appTrail.distinct().size == 1) "" else "s"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
