package com.mayowa.studytracker.presentation.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun StudyPlanScreen(viewModel: StudyPlanViewModel = hiltViewModel()) {
    val plan by viewModel.plan.collectAsState()
    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.fillMaxWidth().height(105.dp)) {
            Box(Modifier.size(130.dp).align(Alignment.TopEnd).blur(40.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha = .18f), RoundedCornerShape(100.dp)))
            Column(Modifier.padding(top = 6.dp)) {
                Text("Today's plan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text("A simple path through your study target.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("SMART PLAN", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .72f), fontWeight = FontWeight.Bold)
                    Text("${plan.totalMinutes} min", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold)
                    Text(plan.message, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .82f))
                }
                Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
        }
        plan.items.forEach { item ->
            ElevatedCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(Modifier.size(46.dp), shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .1f)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Timer, null, tint = MaterialTheme.colorScheme.primary) }
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.subject, fontWeight = FontWeight.ExtraBold)
                        Text(item.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${item.minutes}m", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
