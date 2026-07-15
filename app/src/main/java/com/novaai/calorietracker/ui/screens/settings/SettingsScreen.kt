package com.novaai.calorietracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.navigation.Screen
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

@Composable
fun SettingsScreen(navController: NavController) {
    var metricUnits by remember { mutableStateOf(true) }
    var sounds by remember { mutableStateOf(true) }
    var haptics by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(
            title = stringResource(R.string.placeholder_settings_title),
            onBack = { navController.popBackStack() }
        )

        SectionHeader(
            title = stringResource(R.string.settings_section_preferences),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        NovaCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            cornerRadius = 18.dp
        ) {
            Column {
                NovaToggleRow(
                    icon = Icons.Default.Straighten,
                    title = stringResource(R.string.settings_metric),
                    subtitle = stringResource(R.string.settings_metric_sub),
                    checked = metricUnits,
                    onCheckedChange = { metricUnits = it }
                )
                NovaToggleRow(
                    icon = Icons.Default.VolumeUp,
                    title = stringResource(R.string.settings_sounds),
                    subtitle = stringResource(R.string.settings_sounds_sub),
                    checked = sounds,
                    onCheckedChange = { sounds = it }
                )
                NovaToggleRow(
                    icon = Icons.Default.Vibration,
                    title = stringResource(R.string.settings_haptics),
                    subtitle = stringResource(R.string.settings_haptics_sub),
                    checked = haptics,
                    onCheckedChange = { haptics = it }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        SectionHeader(
            title = stringResource(R.string.settings_section_general),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        NovaCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            cornerRadius = 18.dp
        ) {
            Column {
                SettingsLinkRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.settings_notifications_row),
                    value = stringResource(R.string.settings_notifications_row_sub),
                    onClick = { navController.navigate(Screen.Notifications.route) }
                )
                SettingsInfoRow(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_language),
                    value = stringResource(R.string.settings_language_value)
                )
                SettingsInfoRow(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_version),
                    value = stringResource(R.string.about_version_value)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsLinkRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsIconBox(icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
        }
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = null,
            tint = WhiteAlpha30,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsIconBox(icon)
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = WhiteAlpha60)
    }
}

@Composable
private fun SettingsIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GreenPrimary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
    }
}
