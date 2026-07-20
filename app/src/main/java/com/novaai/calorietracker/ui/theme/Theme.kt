package com.novaai.calorietracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.novaai.calorietracker.data.ThemeMode

/**
 * App-wide theme selection. Initialised from ThemeStore in MainActivity
 * and updated by the Settings screen.
 */
object NovaThemeState {
    var mode by mutableStateOf(ThemeMode.SYSTEM)
}

@Composable
fun NovaAITheme(content: @Composable () -> Unit) {
    val dark = when (NovaThemeState.mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    if (NovaPalette.dark != dark) NovaPalette.dark = dark

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = NavyDeep.toArgb()
            window.navigationBarColor = NavySurface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    // The named colours resolve against the active palette, so the same
    // slot mapping serves both modes.
    val colorScheme = if (dark) {
        darkColorScheme(
            primary          = GreenPrimary,
            onPrimary        = NavyDeep,
            primaryContainer = NavyElevated,
            onPrimaryContainer = GreenPrimary,
            secondary        = GreenLight,
            onSecondary      = NavyDeep,
            secondaryContainer = NavyElevated,
            onSecondaryContainer = GreenLight,
            tertiary         = InfoBlue,
            background       = NavyDeep,
            onBackground     = White,
            surface          = NavySurface,
            onSurface        = White,
            surfaceVariant   = NavyElevated,
            onSurfaceVariant = WhiteAlpha60,
            outline          = NavyBorder,
            error            = ErrorRed,
            onError          = White,
            inversePrimary   = GreenDim,
            scrim            = NavyDeep
        )
    } else {
        lightColorScheme(
            primary          = GreenDim,
            onPrimary        = NavySurface,
            primaryContainer = NavyElevated,
            onPrimaryContainer = GreenDim,
            secondary        = GreenPrimary,
            onSecondary      = NavySurface,
            secondaryContainer = NavyElevated,
            onSecondaryContainer = GreenDim,
            tertiary         = InfoBlue,
            background       = NavyDeep,
            onBackground     = White,
            surface          = NavySurface,
            onSurface        = White,
            surfaceVariant   = NavyElevated,
            onSurfaceVariant = WhiteAlpha60,
            outline          = NavyBorder,
            error            = ErrorRed,
            onError          = NavySurface,
            inversePrimary   = GreenLight,
            scrim            = White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = NovaTypography,
        content     = content
    )
}
