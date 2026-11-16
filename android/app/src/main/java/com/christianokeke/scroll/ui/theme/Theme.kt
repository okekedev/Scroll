package com.christianokeke.scroll.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The iOS app's colours: system teal as the accent, plain system background,
 * grey "secondary background" cards. Values are Apple's system colours for
 * light and dark appearance.
 */
@Immutable
data class ScrollColors(
    val isDark: Boolean,
    val accent: Color,
    val onAccent: Color,
    val background: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val destructive: Color,
    /** Pink, yellow, green, orange, blue, purple, indigo, cyan, mint: one per virtue, in home-grid order. */
    val virtues: List<Color>,
) {
    val accentLight: Color get() = accent.copy(alpha = 0.12f)
    val highlight: Color get() = accent.copy(alpha = if (isDark) 0.22f else 0.14f)
    val destructiveLight: Color get() = destructive.copy(alpha = 0.12f)
    val textTertiary: Color get() = textSecondary.copy(alpha = 0.7f)
    val disabled: Color get() = textSecondary.copy(alpha = 0.4f)
}

private val LightColors = ScrollColors(
    isDark = false,
    accent = Color(0xFF30B0C7),
    onAccent = Color.White,
    background = Color(0xFFFFFFFF),
    card = Color(0xFFF2F2F7),
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF8A8A8E),
    destructive = Color(0xFFFF3B30),
    virtues = listOf(
        Color(0xFFFF2D55), Color(0xFFFFCC00), Color(0xFF34C759),
        Color(0xFFFF9500), Color(0xFF007AFF), Color(0xFFAF52DE),
        Color(0xFF5856D6), Color(0xFF32ADE6), Color(0xFF00C7BE),
    ),
)

private val DarkColors = ScrollColors(
    isDark = true,
    accent = Color(0xFF40C8E0),
    onAccent = Color.White,
    background = Color(0xFF000000),
    card = Color(0xFF1C1C1E),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF98989F),
    destructive = Color(0xFFFF453A),
    virtues = listOf(
        Color(0xFFFF375F), Color(0xFFFFD60A), Color(0xFF30D158),
        Color(0xFFFF9F0A), Color(0xFF0A84FF), Color(0xFFBF5AF2),
        Color(0xFF5E5CE6), Color(0xFF64D2FF), Color(0xFF63E6E2),
    ),
)

private val LocalScrollColors = staticCompositionLocalOf { LightColors }

object ScrollTheme {
    val colors: ScrollColors
        @Composable @ReadOnlyComposable get() = LocalScrollColors.current
}

@Composable
fun ScrollTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    // Material components (menus, progress rings, ripples) pick their colours up from here.
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            secondary = colors.accent,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.background,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.card,
            onSurfaceVariant = colors.textSecondary,
            surfaceContainer = Color(0xFF2C2C2E),
            surfaceContainerHigh = Color(0xFF2C2C2E),
            error = colors.destructive,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            secondary = colors.accent,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.background,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.card,
            onSurfaceVariant = colors.textSecondary,
            surfaceContainer = Color(0xFFFFFFFF),
            surfaceContainerHigh = Color(0xFFFFFFFF),
            error = colors.destructive,
        )
    }
    CompositionLocalProvider(LocalScrollColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
