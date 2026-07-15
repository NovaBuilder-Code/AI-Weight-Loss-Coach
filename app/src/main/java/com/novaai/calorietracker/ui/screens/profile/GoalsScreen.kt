package com.novaai.calorietracker.ui.screens.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.PlaceholderScreen
import com.novaai.calorietracker.ui.theme.GreenPrimary

@Composable
fun GoalsScreen(navController: NavController) {
    PlaceholderScreen(
        navController = navController,
        title = stringResource(R.string.placeholder_goals_title),
        subtitle = stringResource(R.string.placeholder_goals_sub),
        icon = Icons.Default.Flag,
        accentColor = GreenPrimary
    )
}
