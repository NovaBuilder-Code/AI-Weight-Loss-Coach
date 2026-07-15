package com.novaai.calorietracker.ui.screens.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.PlaceholderScreen
import com.novaai.calorietracker.ui.theme.InfoBlue

@Composable
fun PrivacyScreen(navController: NavController) {
    PlaceholderScreen(
        navController = navController,
        title = stringResource(R.string.placeholder_privacy_title),
        subtitle = stringResource(R.string.placeholder_privacy_sub),
        icon = Icons.Default.Security,
        accentColor = InfoBlue
    )
}
