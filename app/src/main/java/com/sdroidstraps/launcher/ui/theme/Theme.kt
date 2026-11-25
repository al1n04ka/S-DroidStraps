package com.sdroidstraps.launcher.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.sdroidstraps.launcher.data.DEFAULT_ACCENT
import com.sdroidstraps.launcher.data.UiStyle

val LocalUiStyle = staticCompositionLocalOf { UiStyle.Classic }

private val BaseDark = darkColorScheme(
    primary = Color(0xFF7C5CFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A2350),
    onPrimaryContainer = Color(0xFFE0D8FF),
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

private fun schemeFor(accent: Color) = BaseDark.copy(
    primary = accent,
    onPrimary = if (accent.luminance() > 0.6f) Color(0xFF07131A) else Color.White,
    primaryContainer = lerp(BaseDark.background, accent, 0.28f),
    onPrimaryContainer = lerp(accent, Color.White, 0.55f)
)

private fun terminalScheme(accent: Color): ColorScheme {
    val text = lerp(accent, Color.White, 0.55f)
    val dim = lerp(accent, Color.Black, 0.35f)
    return darkColorScheme(
        primary = accent,
        onPrimary = Color.Black,
        primaryContainer = lerp(Color.Black, accent, 0.25f),
        onPrimaryContainer = text,
        secondary = text,
        onSecondary = Color.Black,
        secondaryContainer = lerp(Color.Black, accent, 0.2f),
        onSecondaryContainer = text,
        tertiary = lerp(accent, Color.White, 0.3f),
        onTertiary = Color.Black,
        background = Color(0xFF020402),
        onBackground = text,
        surface = Color(0xFF020402),
        onSurface = text,
        surfaceVariant = Color(0xFF071007),
        onSurfaceVariant = dim,
        surfaceContainer = Color(0xFF061006),
        surfaceContainerHigh = Color(0xFF0A160A),
        outline = lerp(Color.Black, accent, 0.45f),
        outlineVariant = lerp(Color.Black, accent, 0.22f),
        error = Color(0xFFFF6B6B),
        onError = Color.Black
    )
}

private fun typographyFor(style: UiStyle): Typography =
    if (style == UiStyle.Terminal) {
        Typography(fontFamily = FontFamily.Monospace)
    } else {
        Typography()
    }

private fun shapesFor(style: UiStyle): Shapes = when (style) {
    UiStyle.Classic -> Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(10.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(28.dp)
    )

    UiStyle.Bloxstrap -> Shapes(
        extraSmall = RoundedCornerShape(6.dp),
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(12.dp),
        extraLarge = RoundedCornerShape(16.dp)
    )

    UiStyle.Minimal, UiStyle.Terminal -> Shapes(
        extraSmall = RoundedCornerShape(0.dp),
        small = RoundedCornerShape(0.dp),
        medium = RoundedCornerShape(0.dp),
        large = RoundedCornerShape(0.dp),
        extraLarge = RoundedCornerShape(0.dp)
    )
}

@Composable
fun SDroidStrapsTheme(
    accent: Long = DEFAULT_ACCENT,
    style: UiStyle = UiStyle.Classic,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalUiStyle provides style) {
        MaterialTheme(
            colorScheme = if (style == UiStyle.Terminal) {
                terminalScheme(Color(accent))
            } else {
                schemeFor(Color(accent))
            },
            typography = typographyFor(style),
            shapes = shapesFor(style),
            content = content
        )
    }
}
