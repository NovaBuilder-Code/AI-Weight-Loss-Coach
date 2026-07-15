package com.novaai.calorietracker.ui.screens.meals

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.PlaceholderScreen
import com.novaai.calorietracker.ui.theme.InfoBlue

@Composable
fun MealSuggestionsScreen(navController: NavController) {
    PlaceholderScreen(
        navController = navController,
        title = stringResource(R.string.placeholder_meal_suggestions_title),
        subtitle = stringResource(R.string.placeholder_meal_suggestions_sub),
        icon = Icons.Default.RestaurantMenu,
        accentColor = InfoBlue,
        showNovaAvatar = true
    )
}
