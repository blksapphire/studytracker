package com.mayowa.studytracker.presentation.insights

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayowa.studytracker.presentation.dashboard.formatDuration

// Note: "peak focus hours" from the original feature list needs hourly-bucketed
// session data, which the current schema doesn't retain (only day-level
// daily_stats + per-session start/end). Revisit once History/Insights need
// that granularity — cheap to add to the rollup worker later.
@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val insights by viewModel.weeklyInsights.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("This week", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        InsightRow("Total study time", formatDuration(insights.totalMillis))
        InsightRow("Daily average", formatDuration(insights.averageMillisPerDay))
        insights.bestDay?.let { InsightRow("Best day", "$it (${formatDuration(insights.bestDayMillis)})") }
        insights.topTag?.let { InsightRow("Most-studied subject", it) }
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
