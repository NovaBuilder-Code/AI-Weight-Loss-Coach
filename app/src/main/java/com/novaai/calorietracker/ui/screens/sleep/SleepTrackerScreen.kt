package com.novaai.calorietracker.ui.screens.sleep

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.PlaceholderScreen

@Composable
fun SleepTrackerScreen(navController: NavController) {
    PlaceholderScreen(
        navController = navController,
        title = stringResource(R.string.placeholder_sleep_title),
        subtitle = stringResource(R.string.placeholder_sleep_sub),
        icon = Icons.Default.NightsStay,
        accentColor = Color(0xFF9B8FFF),
        showNovaAvatar = true
    )
}
