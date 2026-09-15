package com.mayowa.studytracker.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Noticeably rounder than Material defaults — cards, chips, and buttons
// read as friendlier/game-like. Kept short of fully pill-shaped so it
// doesn't undercut "premium enough to trust with a study habit."
val StudyTrackerShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)
