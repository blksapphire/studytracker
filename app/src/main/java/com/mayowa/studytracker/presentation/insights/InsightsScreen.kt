package com.mayowa.studytracker.presentation.insights

import androidx.compose.foundation.layout.*
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

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
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
