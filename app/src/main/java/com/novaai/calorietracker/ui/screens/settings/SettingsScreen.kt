package com.novaai.calorietracker.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.PlaceholderScreen
import com.novaai.calorietracker.ui.theme.GreenPrimary

@Composable
fun SettingsScreen(navController: NavController) {
    PlaceholderScreen(
        navController = navController,
        title = stringResource(R.string.placeholder_settings_title),
        subtitle = stringResource(R.string.placeholder_settings_sub),
        icon = Icons.Default.Settings,
        accentColor = GreenPrimary,
        showNovaAvatar = true
    )
}
