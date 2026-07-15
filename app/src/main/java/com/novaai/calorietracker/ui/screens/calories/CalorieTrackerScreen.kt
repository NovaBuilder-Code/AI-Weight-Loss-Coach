package com.novaai.calorietracker.ui.screens.calories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.PlaceholderScreen
import com.novaai.calorietracker.ui.theme.WarningAmber

@Composable
fun CalorieTrackerScreen(navController: NavController) {
    PlaceholderScreen(
        navController = navController,
        title = stringResource(R.string.placeholder_calorie_tracker_title),
        subtitle = stringResource(R.string.placeholder_calorie_tracker_sub),
        icon = Icons.Default.LocalFireDepartment,
        accentColor = WarningAmber
    )
}
