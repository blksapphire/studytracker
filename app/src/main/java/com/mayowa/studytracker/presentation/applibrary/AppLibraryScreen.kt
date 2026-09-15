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
import com.mayowa.studytracker.data.tracking.AppCategory

@Composable
fun AppLibraryScreen(viewModel: AppLibraryViewModel = hiltViewModel()) {
    val apps by viewModel.apps.collectAsState()
    var selectedCategory by remember { mutableStateOf<AppCategory?>(null) }
    var search by remember { mutableStateOf("") }

    val visibleApps = apps.filter { app ->
        (selectedCategory == null || app.category == selectedCategory) &&
            (search.isBlank() || app.label.contains(search, ignoreCase = true))
    }
    val distractingCount = apps.count { it.isDistracting }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Box(Modifier.fillMaxWidth().height(112.dp)) {
                Box(Modifier.size(110.dp).align(Alignment.TopEnd).blur(40.dp).background(MaterialTheme.colorScheme.error.copy(alpha = .13f), CircleShape))
                Column(Modifier.padding(top = 8.dp)) {
                    Text("App library", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text("Teach StudyTracker what helps you focus.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("Your focus rules", fontWeight = FontWeight.Bold)
                        Text("$distractingCount app${if (distractingCount == 1) "" else "s"} marked as distracting", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text("Search your apps") }
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = selectedCategory == null, onClick = { selectedCategory = null }, label = { Text("All") })
                AppCategory.entries.take(3).forEach { category ->
                    FilterChip(selected = selectedCategory == category, onClick = { selectedCategory = category }, label = { Text(category.label) })
                }
            }
        }

        item {
            Text("${visibleApps.size} apps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 7.dp, bottom = 2.dp))
        }

        items(visibleApps, key = { it.packageName }) { app ->
            AppRow(app, onToggle = { viewModel.toggleDistracting(app.packageName) }, onCategory = { viewModel.setCategory(app.packageName, it) })
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledAppUi,
    onToggle: () -> Unit,
    onCategory: (AppCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (app.isDistracting) MaterialTheme.colorScheme.errorContainer.copy(alpha = .48f) else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = if (app.isDistracting) 1.dp else 0.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(if (app.isDistracting) MaterialTheme.colorScheme.error.copy(alpha = .1f) else MaterialTheme.colorScheme.primary.copy(alpha = .08f)), contentAlignment = Alignment.Center) {
                    Icon(if (app.isDistracting) Icons.Filled.Block else Icons.Filled.CheckCircle, null, tint = if (app.isDistracting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
                Column(Modifier.weight(1f)) {
                    Text(app.label, fontWeight = FontWeight.Bold)
                    Text(app.category.label + if (app.isDistracting) " • Distraction" else " • Study time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = app.isDistracting, onCheckedChange = { onToggle() })
            }

            TextButton(onClick = { expanded = !expanded }, modifier = Modifier.padding(start = 50.dp)) {
                Text(if (expanded) "Hide category" else "Change category")
            }
            if (expanded) {
                Column(Modifier.padding(start = 50.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppCategory.entries.forEach { category ->
                        DropdownCategoryRow(category, selected = category == app.category) { onCategory(category); expanded = false }
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownCategoryRow(category: AppCategory, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category.label)
            if (selected) Text("✓", fontWeight = FontWeight.Bold)
        }
    }
}
