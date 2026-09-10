package com.kharcha.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = DarkBackground,
    primaryContainer = TealDark,
    secondary = PurpleAccent,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    error = ExpenseRed,
    outline = GlassBorder,
    outlineVariant = DarkCardVariant
)

private val LightColorScheme = lightColorScheme(
    primary = TealDark,
    onPrimary = LightBackground,
    primaryContainer = TealLight,
    secondary = PurpleAccent,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightCardVariant,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    error = ExpenseRedDark,
    outline = GlassBorderLight,
    outlineVariant = LightCardVariant
)

@Suppress("DEPRECATION")
@Composable
fun KharchaTheme(
    darkTheme: Boolean = true, // Default to Dark Theme for Sleek Dark Modern experience
    dynamicColor: Boolean = false, // Keep Kharcha's signature aesthetic on all devices
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KharchaTypography,
        content = content
    )
}
