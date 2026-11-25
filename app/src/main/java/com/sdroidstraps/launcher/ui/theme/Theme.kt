package com.sdroidstraps.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StrapDark = darkColorScheme(
    primary = Color(0xFF5CC8FF),
    onPrimary = Color(0xFF001E2B),
    primaryContainer = Color(0xFF00405C),
    onPrimaryContainer = Color(0xFFC6E7FF),
    secondary = Color(0xFF88D6B0),
    onSecondary = Color(0xFF00210F),
    secondaryContainer = Color(0xFF005234),
    onSecondaryContainer = Color(0xFFA6F2CB),
    tertiary = Color(0xFFFFD08A),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE2E6EA),
    surface = Color(0xFF121820),
    onSurface = Color(0xFFE2E6EA),
    surfaceVariant = Color(0xFF1B232D),
    onSurfaceVariant = Color(0xFFA9B4C0),
    surfaceContainer = Color(0xFF161D26),
    surfaceContainerHigh = Color(0xFF1B232D),
    outline = Color(0xFF3A4652),
    outlineVariant = Color(0xFF28323D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun SDroidStrapsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StrapDark,
        typography = Typography(),
        content = content
    )
}
