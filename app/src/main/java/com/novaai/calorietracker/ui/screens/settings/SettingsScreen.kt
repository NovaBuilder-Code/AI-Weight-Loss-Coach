package com.novaai.calorietracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.NovaTopBar
import com.novaai.calorietracker.ui.theme.NavyDeep
import com.novaai.calorietracker.ui.theme.WhiteAlpha60

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        NovaTopBar(
            title = stringResource(R.string.placeholder_settings_title),
            onBack = { navController.popBackStack() }
        )

        Text(
            text = stringResource(R.string.settings_more_options),
            style = MaterialTheme.typography.bodyMedium,
            color = WhiteAlpha60,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}
