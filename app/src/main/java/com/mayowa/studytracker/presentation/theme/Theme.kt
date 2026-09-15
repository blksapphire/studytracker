package com.mayowa.studytracker.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,

    secondary = StreakCoral,
    onSecondary = SurfaceLight,
    secondaryContainer = StreakCoralContainer,
    onSecondaryContainer = Color(0xFF5C1B0A), // deep coral-brown, readable on the light coral container

    tertiary = XpAmber,
    onTertiary = Color(0xFF3D2E00),
    tertiaryContainer = XpAmberContainer,
    onTertiaryContainer = Color(0xFF3D2E00),

    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFDCE5E3),
    onSurfaceVariant = Color(0xFF3F4947),

    error = ErrorRed,
    onError = SurfaceLight
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF5FE0CE), // lighter teal for contrast on dark surfaces
    onPrimary = Color(0xFF00382F),
    primaryContainer = TealPrimaryDark,
    onPrimaryContainer = TealPrimaryContainer,

    secondary = StreakCoral,
    onSecondary = Color(0xFF3D0F04),
    secondaryContainer = Color(0xFF7A2A16),
    onSecondaryContainer = StreakCoralContainer,

    tertiary = XpAmber,
    onTertiary = Color(0xFF3D2E00),
    tertiaryContainer = Color(0xFF5A4400),
    onTertiaryContainer = XpAmberContainer,

    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF3F4947),
    onSurfaceVariant = Color(0xFFBFC9C6),

    error = ErrorRed,
    onError = Color(0xFF3D0007)
)

@Composable
fun StudyTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = StudyTrackerTypography,
        shapes = StudyTrackerShapes,
        content = content
    )
}
