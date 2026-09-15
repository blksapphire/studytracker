package com.mayowa.studytracker.presentation.insights

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

@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val insights by viewModel.weeklyInsights.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        item {
            Text("This week", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            InsightRow("Total study time", formatDuration(insights.totalMillis))
            InsightRow("Daily average", formatDuration(insights.averageMillisPerDay))
            InsightRow("Sessions", insights.sessionCount.toString())
            InsightRow("Average session", formatDuration(insights.averageSessionMillis))
            insights.bestDay?.let { InsightRow("Best day", "$it (${formatDuration(insights.bestDayMillis)})") }
            insights.topTag?.let { InsightRow("Most-studied subject", it) }
            insights.peakHour?.let { hour ->
                val end = (hour + 1) % 24
                InsightRow("Peak focus hour", "%02d:00–%02d:00".format(hour, end))
            }
            Spacer(Modifier.height(20.dp))
            Text("Top study apps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }

        if (insights.topApps.isEmpty()) {
            item {
                Text("Use a few study sessions and your tracked apps will appear here.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(insights.topApps) { app ->
                ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.label, fontWeight = FontWeight.SemiBold)
                            Text(app.packageName, style = MaterialTheme.typography.labelSmall)
                        }
                        Text(formatDuration(app.trackedMillis), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider()
}
