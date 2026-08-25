package com.novaai.calorietracker.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.NotificationPrefs
import com.novaai.calorietracker.data.NotificationPrefsStore
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

@Composable
fun NotificationsScreen(navController: NavController) {
    val context = LocalContext.current
    var prefs by remember { mutableStateOf(NotificationPrefsStore.load(context)) }

    fun update(transform: (NotificationPrefs) -> NotificationPrefs) {
        val updated = transform(prefs)
        NotificationPrefsStore.save(context, updated)
        prefs = updated
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(
            title = stringResource(R.string.placeholder_notifications_title),
            onBack = { navController.popBackStack() }
        )

        SectionHeader(
            title = stringResource(R.string.notif_section),
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
                    icon = Icons.Default.RestaurantMenu,
                    title = stringResource(R.string.notif_meals),
                    subtitle = stringResource(R.string.notif_meals_sub),
                    checked = prefs.meals,
                    onCheckedChange = { newValue -> update { it.copy(meals = newValue) } }
                )
                NovaToggleRow(
                    icon = Icons.Default.WaterDrop,
                    title = stringResource(R.string.notif_water),
                    subtitle = stringResource(R.string.notif_water_sub),
                    checked = prefs.water,
                    onCheckedChange = { newValue -> update { it.copy(water = newValue) } },
                    accentColor = Color(0xFF40CFFF)
                )
                NovaToggleRow(
                    icon = Icons.Default.MonitorWeight,
                    title = stringResource(R.string.notif_weighin),
                    subtitle = stringResource(R.string.notif_weighin_sub),
                    checked = prefs.weighIn,
                    onCheckedChange = { newValue -> update { it.copy(weighIn = newValue) } },
                    accentColor = InfoBlue
                )
                NovaToggleRow(
                    icon = Icons.Default.DirectionsWalk,
                    title = stringResource(R.string.notif_steps),
                    subtitle = stringResource(R.string.notif_steps_sub),
                    checked = prefs.steps,
                    onCheckedChange = { newValue -> update { it.copy(steps = newValue) } }
                )
                NovaToggleRow(
                    icon = Icons.Default.EmojiEmotions,
                    title = stringResource(R.string.notif_motivation),
                    subtitle = stringResource(R.string.notif_motivation_sub),
                    checked = prefs.motivation,
                    onCheckedChange = { newValue -> update { it.copy(motivation = newValue) } },
                    accentColor = WarningAmber
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
