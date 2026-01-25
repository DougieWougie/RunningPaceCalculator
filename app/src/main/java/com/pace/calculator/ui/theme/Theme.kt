package com.pace.calculator.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    primaryContainer = LightAccent,
    onPrimaryContainer = Color.White,
    secondary = LightBackgroundSecondary,
    onSecondary = LightTextPrimary,
    secondaryContainer = LightBackgroundSecondary,
    onSecondaryContainer = LightTextSecondary,
    tertiary = LightTextMuted,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightBackgroundSecondary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = Color.White,
    primaryContainer = DarkAccent,
    onPrimaryContainer = Color.White,
    secondary = DarkBackgroundSecondary,
    onSecondary = DarkTextPrimary,
    secondaryContainer = DarkBackgroundSecondary,
    onSecondaryContainer = DarkTextSecondary,
    tertiary = DarkTextMuted,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBackgroundSecondary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder
)

data class PaceColors(
    val textMuted: Color,
    val pulseColor: Color
)

private val LightPaceColors = PaceColors(textMuted = LightTextMuted, pulseColor = LightPulse)
private val DarkPaceColors = PaceColors(textMuted = DarkTextMuted, pulseColor = DarkPulse)

val LocalPaceColors = compositionLocalOf { LightPaceColors }

@Composable
fun PaceCalculatorTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val paceColors = if (darkTheme) DarkPaceColors else LightPaceColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalPaceColors provides paceColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
