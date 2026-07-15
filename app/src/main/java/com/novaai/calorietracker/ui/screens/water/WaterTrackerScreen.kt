package com.novaai.calorietracker.ui.screens.water

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.PlaceholderScreen

@Composable
fun WaterTrackerScreen(navController: NavController) {
    PlaceholderScreen(
        navController = navController,
        title = stringResource(R.string.placeholder_water_title),
        subtitle = stringResource(R.string.placeholder_water_sub),
        icon = Icons.Default.WaterDrop,
        accentColor = Color(0xFF40CFFF),
        showNovaAvatar = true
    )
}
