package com.mayowa.studytracker.presentation.goals

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayowa.studytracker.presentation.dashboard.formatDuration
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = state.examDateMillis ?: System.currentTimeMillis() }
        DatePickerDialog(
            androidx.compose.ui.platform.LocalContext.current,
            { _, year, month, day ->
                val picked = Calendar.getInstance().apply { set(year, month, day, 23, 59, 59); set(Calendar.MILLISECOND, 999) }
                viewModel.setExamDate(picked.timeInMillis)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).apply { setOnDismissListener { showDatePicker = false } }.show()
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(Modifier.fillMaxWidth().height(110.dp)) {
                Box(Modifier.size(120.dp).align(Alignment.TopEnd).blur(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .18f), CircleShape))
                Column(Modifier.padding(top = 8.dp)) {
                    Text("Your goals", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text("A plan that adapts to the student you are becoming.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        Icon(Icons.Filled.TrackChanges, null, tint = MaterialTheme.colorScheme.onPrimary)
                        Text("DAILY TARGET", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .8f))
                    }
                    Text(formatDuration(state.adaptiveGoalMillis), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary)
                    Text("Adaptive target · your recent rhythm is taken into account.", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .82f))
                }
            }
        }
        item {
            ElevatedCard(shape = RoundedCornerShape(26.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(19.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.tertiary.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Flag, null, tint = MaterialTheme.colorScheme.tertiary) }
                        Column(Modifier.weight(1f)) {
                            Text("Exam countdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Text("Keep the finish line visible.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (state.examDateMillis == null) {
                        Text("No exam date yet. Add one and StudyTracker will keep the countdown on your radar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("Choose exam date") }
                    } else {
                        val formatted = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date(state.examDateMillis))
                        Text("${state.daysUntilExam ?: 0}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                        Text(if (state.daysUntilExam == 1) "day until your exam" else "days until your exam", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatted, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("Change date") }
                            TextButton(onClick = viewModel::clearExamDate) { Text("Clear") }
                        }
                    }
                }
            }
        }
        item {
            Text("Today's study plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }
        if (state.plan.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .7f), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.secondary)
                        Column {
                            Text("Add your subjects", fontWeight = FontWeight.Bold)
                            Text("Your daily target will be split into focused blocks automatically.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        } else {
            items(state.plan.size) { index ->
                val item = state.plan[index]
                ElevatedCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                        Surface(Modifier.size(46.dp), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.primary) } }
                        Column(Modifier.weight(1f)) {
                            Text(item.subject, fontWeight = FontWeight.Bold)
                            Text(item.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${item.minutes}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .7f), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = MaterialTheme.colorScheme.secondary)
                    Column {
                        Text("Consistency beats cramming", fontWeight = FontWeight.Bold)
                        Text("Your plan changes as your study history changes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    }
}
