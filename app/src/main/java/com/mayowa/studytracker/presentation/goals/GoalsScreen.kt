package com.mayowa.studytracker.presentation.goals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayowa.studytracker.presentation.dashboard.formatDuration
import java.text.SimpleDateFormat
import java.util.*

// Date-picker dialog is a platform-native component (no clean pure-Compose
// equivalent without a third-party lib) — stubbed with a "+7 days" quick
// action for now; swap in a real DatePickerDialog during the UI polish pass.
@Composable
fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Your goal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "${formatDuration(state.adaptiveGoalMillis)} / day",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            "Adjusts automatically based on your last week — hit it consistently and it nudges up.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(32.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        Text("Exam countdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (state.examDateMillis == null) {
            Text("No exam date set.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val in14Days = System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000
                viewModel.setExamDate(in14Days)
            }) {
                Text("Set exam in 14 days (placeholder)")
            }
        } else {
            val formatted = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(state.examDateMillis!!))
            Text("$formatted — ${state.daysUntilExam} days left", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { viewModel.clearExamDate() }) { Text("Clear") }
        }
    }
}
