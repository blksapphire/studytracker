package com.mayowa.studytracker.presentation.session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayowa.studytracker.presentation.dashboard.formatDuration

// Starter set — same idea as AppClassifier's default list: seeded, user-editable later.
private val SUGGESTED_TAGS = listOf("General", "Reading", "Coursework", "Exam prep", "Coding")

@Composable
fun SessionScreen(viewModel: SessionViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!state.isLive) {
            Text("No session in progress", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Put your phone down and step away from distracting apps — tracking starts automatically.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text("Studying", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                formatDuration(state.elapsedMillis),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(32.dp))
            Text("What are you working on?", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SUGGESTED_TAGS) { tag ->
                    FilterChip(
                        selected = state.currentTag == tag,
                        onClick = { viewModel.tagCurrentSession(tag) },
                        label = { Text(tag) }
                    )
                }
            }
        }
    }
}
