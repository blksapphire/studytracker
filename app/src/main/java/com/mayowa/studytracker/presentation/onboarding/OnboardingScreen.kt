package com.mayowa.studytracker.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mayowa.studytracker.data.profile.ProfileStore
import com.mayowa.studytracker.data.tracking.TrackingService
import com.mayowa.studytracker.data.tracking.UsagePermissionHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val profileStore = remember { ProfileStore(context) }
    val scope = rememberCoroutineScope()
    var hasUsageAccess by remember { mutableStateOf(UsagePermissionHelper.hasUsageAccess(context)) }
    var step by remember { mutableIntStateOf(if (hasUsageAccess) 1 else 0) }
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("Build a consistent study routine") }
    var dailyTarget by remember { mutableIntStateOf(60) }
    var examDate by remember { mutableStateOf<Long?>(null) }
    var nudgeEnabled by remember { mutableStateOf(true) }
    val subjects = remember { mutableStateListOf<String>() }
    val subjectOptions = listOf("Math", "Physics", "Chemistry", "Biology", "English", "Computer Science", "Business", "Other")

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.size(180.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-30).dp).blur(65.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .16f), CircleShape))
        Box(Modifier.size(150.dp).align(Alignment.BottomStart).offset(x = (-35).dp, y = 40.dp).blur(55.dp).background(MaterialTheme.colorScheme.tertiary.copy(alpha = .14f), CircleShape))
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(19.dp))
                }
                Text("StudyTracker", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { repeat(2) { StepDot(active = it == step) } }
                when (step) {
                    0 -> PermissionStep(
                        icon = Icons.Filled.Visibility,
                        eyebrow = "QUICK SETUP",
                        title = "Know where your study time goes.",
                        body = "StudyTracker watches which app is open so it can separate focused study from distractions. Your activity stays on your phone.",
                        button = "Give usage access",
                        onButton = { context.startActivity(UsagePermissionHelper.requestUsageAccessIntent()) },
                        secondary = "I already did this",
                        onSecondary = { hasUsageAccess = UsagePermissionHelper.hasUsageAccess(context); if (hasUsageAccess) step = 1 }
                    )
                    1 -> ProfileStep(
                        name = name, onNameChange = { name = it }, goal = goal, onGoalChange = { goal = it },
                        subjects = subjects, subjectOptions = subjectOptions, dailyTarget = dailyTarget, onTargetChange = { dailyTarget = it },
                        examDate = examDate, onExamDateChange = { examDate = it }, nudgeEnabled = nudgeEnabled, onNudgeChange = { nudgeEnabled = it },
                        onSave = {
                            scope.launch {
                                profileStore.save(name, subjects, goal, examDate, dailyTarget, nudgeEnabled)
                                TrackingService.start(context)
                                onFinished()
                            }
                        }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Icon(Icons.Filled.Lock, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Your study history stays on your device.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProfileStep(
    name: String, onNameChange: (String) -> Unit, goal: String, onGoalChange: (String) -> Unit,
    subjects: MutableList<String>, subjectOptions: List<String>, dailyTarget: Int, onTargetChange: (Int) -> Unit,
    examDate: Long?, onExamDateChange: (Long?) -> Unit, nudgeEnabled: Boolean, onNudgeChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("MAKE IT YOURS", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("A study plan that fits you.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(value = name, onValueChange = onNameChange, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Your name") }, placeholder = { Text("What should we call you?") }, shape = RoundedCornerShape(16.dp))
        OutlinedTextField(value = goal, onValueChange = onGoalChange, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Main study goal") }, shape = RoundedCornerShape(16.dp))
        Text("Subjects", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            items(subjectOptions) { subject ->
                FilterChip(selected = subject in subjects, onClick = { if (subject in subjects) subjects.remove(subject) else subjects.add(subject) }, label = { Text(subject) })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) { Text("Daily target", fontWeight = FontWeight.SemiBold); Text("${dailyTarget} minutes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Slider(value = dailyTarget.toFloat(), onValueChange = { onTargetChange(it.toInt()) }, valueRange = 15f..240f, steps = 14, modifier = Modifier.weight(1.4f))
        }
        OutlinedButton(onClick = {
            val calendar = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(context, { _, y, m, d -> calendar.set(y, m, d, 12, 0, 0); onExamDateChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Filled.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text(if (examDate == null) "Add exam date" else "Exam: ${SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(examDate!!))}")
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) { Text("Focus nudges", fontWeight = FontWeight.SemiBold); Text("Gentle reminders when distraction starts.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Switch(checked = nudgeEnabled, onCheckedChange = onNudgeChange)
        }
        Button(onClick = onSave, enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(17.dp)) { Text("Build my study space", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun StepDot(active: Boolean) {
    Box(Modifier.height(6.dp).width(if (active) 28.dp else 10.dp).clip(RoundedCornerShape(10.dp)).background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant))
}

@Composable
private fun PermissionStep(icon: androidx.compose.ui.graphics.vector.ImageVector, eyebrow: String, title: String, body: String, button: String, onButton: () -> Unit, secondary: String, onSecondary: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = .1f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp)) }
        Text(eyebrow, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
        Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onButton, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) { Text(button, fontWeight = FontWeight.Bold) }
        TextButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth()) { Text(secondary) }
    }
}
