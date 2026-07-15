package com.novaai.calorietracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NovaDarkColorScheme = darkColorScheme(
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

@Composable
fun NovaAITheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = NavyDeep.toArgb()
            window.navigationBarColor = NavySurface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = NovaDarkColorScheme,
        typography  = NovaTypography,
        content     = content
    )
}
