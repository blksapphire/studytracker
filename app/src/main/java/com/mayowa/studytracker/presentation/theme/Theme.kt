package com.mayowa.studytracker.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealPrimaryContainer,
    onPrimaryContainer = TealOnPrimaryContainer,
    secondary = MintAccent,
    onSecondary = Color(0xFF06281D),
    secondaryContainer = MintContainer,
    onSecondaryContainer = Color(0xFF063B2B),
    tertiary = XpAmber,
    onTertiary = Color(0xFF3B2B00),
    tertiaryContainer = XpAmberContainer,
    onTertiaryContainer = Color(0xFF3B2B00),
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFE9EAF2),
    onSurfaceVariant = Color(0xFF555765),
    error = ErrorRed,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFAAA6FF),
    onPrimary = Color(0xFF29246B),
    primaryContainer = TealPrimaryDark,
    onPrimaryContainer = Color(0xFFE9E7FF),
    secondary = Color(0xFF65E5BA),
    onSecondary = Color(0xFF00382A),
    secondaryContainer = Color(0xFF07513D),
    onSecondaryContainer = MintContainer,
    tertiary = XpAmber,
    onTertiary = Color(0xFF3B2B00),
    tertiaryContainer = Color(0xFF5A4400),
    onTertiaryContainer = XpAmberContainer,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF282A36),
    onSurfaceVariant = Color(0xFFC3C4D0),
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
