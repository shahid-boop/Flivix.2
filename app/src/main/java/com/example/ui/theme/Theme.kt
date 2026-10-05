package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FylvixDarkColorScheme = darkColorScheme(
    primary = FylvixCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4C0519),
    onPrimaryContainer = Color(0xFFFFD9E0),
    secondary = FylvixGold,
    onSecondary = Color(0xFF1E1300),
    secondaryContainer = Color(0xFF3B2A07),
    onSecondaryContainer = Color(0xFFFFE08A),
    tertiary = FylvixCyan,
    onTertiary = Color(0xFF002631),
    background = FylvixObsidian,
    onBackground = FylvixTextPrimary,
    surface = FylvixSurfaceDark,
    onSurface = FylvixTextPrimary,
    surfaceVariant = FylvixSurfaceElevated,
    onSurfaceVariant = FylvixTextSecondary,
    outline = FylvixGlassBorder
)

private val FylvixMidnightGoldScheme = darkColorScheme(
    primary = FylvixGold,
    onPrimary = Color(0xFF1A1100),
    primaryContainer = Color(0xFF3E2C05),
    onPrimaryContainer = Color(0xFFFFE599),
    secondary = FylvixCrimsonBright,
    onSecondary = Color.White,
    tertiary = FylvixCyan,
    background = Color(0xFF050608),
    onBackground = FylvixTextPrimary,
    surface = Color(0xFF0E1117),
    onSurface = FylvixTextPrimary,
    surfaceVariant = Color(0xFF171C26),
    onSurfaceVariant = FylvixTextSecondary,
    outline = Color(0xFF2E3646)
)

private val FylvixLightColorScheme = lightColorScheme(
    primary = FylvixCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E0),
    onPrimaryContainer = Color(0xFF3F0010),
    secondary = Color(0xFFB45309),
    onSecondary = Color.White,
    tertiary = Color(0xFF0284C7),
    background = FylvixLightBg,
    onBackground = FylvixLightText,
    surface = FylvixLightSurface,
    onSurface = FylvixLightText,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun FylvixTheme(
    themeMode: String = "Cinematic Dark",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "Midnight Gold" -> FylvixMidnightGoldScheme
        "Studio Light" -> FylvixLightColorScheme
        else -> FylvixDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FylvixTheme(
        themeMode = if (darkTheme) "Cinematic Dark" else "Studio Light",
        content = content
    )
}
