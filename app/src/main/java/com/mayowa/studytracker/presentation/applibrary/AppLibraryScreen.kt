package com.mayowa.studytracker.presentation.applibrary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppLibraryScreen(viewModel: AppLibraryViewModel = hiltViewModel()) {
    val apps by viewModel.apps.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Toggle which apps count as distractions. Everything else counts toward study time.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(apps, key = { it.packageName }) { app ->
                ListItem(
                    headlineContent = { Text(app.label) },
                    supportingContent = { Text(app.packageName, style = MaterialTheme.typography.labelSmall) },
                    trailingContent = {
                        Switch(
                            checked = app.isDistracting,
                            onCheckedChange = { viewModel.toggleDistracting(app.packageName) }
                        )
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
