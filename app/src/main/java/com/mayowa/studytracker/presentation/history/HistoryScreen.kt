package com.mayowa.studytracker.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayowa.studytracker.presentation.dashboard.formatDuration

/**
 * Plain per-day list for now. A calendar heatmap view (GitHub-contributions
 * style) is the obvious "standout" upgrade here — flagged for the design
 * pass rather than built with placeholder colors.
 */
@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val dailyStats by viewModel.dailyStats.collectAsState()

    if (dailyStats.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No history yet — your first session will show up here.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(dailyStats) { day ->
            ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(day.date, fontWeight = FontWeight.Bold)
                        Text(
                            "${day.sessionCount} session${if (day.sessionCount == 1) "" else "s"}" +
                                (day.topTag?.let { " · $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(formatDuration(day.totalStudyMillis), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
