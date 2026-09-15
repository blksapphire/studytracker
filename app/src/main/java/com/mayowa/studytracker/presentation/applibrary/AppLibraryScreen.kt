package com.mayowa.studytracker.presentation.applibrary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppLibraryScreen(viewModel: AppLibraryViewModel = hiltViewModel()) {
    val apps by viewModel.apps.collectAsState()
    val distractingCount = apps.count { it.isDistracting }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Box(Modifier.fillMaxWidth().height(104.dp)) {
                    Box(Modifier.size(100.dp).align(Alignment.TopEnd).blur(38.dp).background(MaterialTheme.colorScheme.error.copy(alpha = .13f), CircleShape))
                    Column(Modifier.padding(top = 8.dp)) {
                        Text("App library", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                        Text("Choose what should count as a distraction.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Tune, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Your rules", fontWeight = FontWeight.Bold)
                            Text("$distractingCount app${if (distractingCount == 1) "" else "s"} marked as distracting", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Text("Installed apps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 7.dp, bottom = 2.dp))
            }

            items(apps, key = { it.packageName }) { app ->
                Surface(shape = RoundedCornerShape(20.dp), color = if (app.isDistracting) MaterialTheme.colorScheme.errorContainer.copy(alpha = .48f) else MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth(), tonalElevation = if (app.isDistracting) 1.dp else 0.dp) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(44.dp).clip(CircleShape).background(if (app.isDistracting) MaterialTheme.colorScheme.error.copy(alpha = .1f) else MaterialTheme.colorScheme.primary.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                            Icon(if (app.isDistracting) Icons.Filled.Block else Icons.Filled.CheckCircle, null, tint = if (app.isDistracting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(app.label, fontWeight = FontWeight.Bold)
                            Text(if (app.isDistracting) "Distraction" else "Counts toward study", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = app.isDistracting, onCheckedChange = { viewModel.toggleDistracting(app.packageName) })
                    }
                }
            }
        }
    }
}
