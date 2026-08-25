package com.novaai.calorietracker.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.ActivityLevel
import com.novaai.calorietracker.data.MainGoal
import com.novaai.calorietracker.data.MeasurementUnits
import com.novaai.calorietracker.data.ProfileDisplay
import com.novaai.calorietracker.data.UnitConversion
import com.novaai.calorietracker.data.UserProfile
import com.novaai.calorietracker.data.UserProfileStore
import com.novaai.calorietracker.navigation.Screen
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val profile = remember { UserProfileStore.load(context) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val accountItems = listOf(
        ProfileMenuItemData(Icons.Default.Person, stringResource(R.string.profile_item_personal_info), stringResource(R.string.profile_item_personal_info_sub)) {
            navController.navigate(Screen.PersonalInfo.route)
        },
        ProfileMenuItemData(Icons.Default.Edit, stringResource(R.string.profile_edit_title), stringResource(R.string.profile_edit_menu_sub)) {
            navController.navigate(Screen.EditProfile.route)
        },
        ProfileMenuItemData(Icons.Default.Flag, stringResource(R.string.profile_item_goals), stringResource(R.string.profile_item_goals_sub)) {
            navController.navigate(Screen.Goals.route)
        },
        ProfileMenuItemData(Icons.Default.Notifications, stringResource(R.string.profile_item_notifications), stringResource(R.string.profile_item_notifications_sub)) {
            navController.navigate(Screen.Notifications.route)
        },
    )

    val appItems = listOf(
        ProfileMenuItemData(Icons.Default.Star, stringResource(R.string.profile_item_premium), stringResource(R.string.profile_item_premium_sub)) {
            navController.navigate(Screen.Subscription.route)
        },
        ProfileMenuItemData(Icons.Default.Settings, stringResource(R.string.profile_item_settings), stringResource(R.string.profile_item_settings_sub)) {
            navController.navigate(Screen.Settings.route)
        },
        ProfileMenuItemData(Icons.Default.Security, stringResource(R.string.profile_item_privacy), stringResource(R.string.profile_item_privacy_sub)) {
            navController.navigate(Screen.Privacy.route)
        },
        ProfileMenuItemData(Icons.Default.HelpOutline, stringResource(R.string.profile_item_help), stringResource(R.string.profile_item_help_sub)) {
            navController.navigate(Screen.HelpSupport.route)
        },
        ProfileMenuItemData(Icons.Default.Info, stringResource(R.string.profile_item_about), stringResource(R.string.profile_item_about_sub)) {
            navController.navigate(Screen.About.route)
        },
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            NovaTopBar(title = stringResource(R.string.profile_title), onBack = { navController.popBackStack() })
        }
        item { ProfileAvatar(profile) }
        item { Spacer(Modifier.height(20.dp)) }
        item { ProfileStatsRow(profile) }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            SectionHeader(
                title = stringResource(R.string.profile_section_account),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            ProfileMenuGroup(items = accountItems)
        }
        item { Spacer(Modifier.height(16.dp)) }
        item {
            SectionHeader(
                title = stringResource(R.string.profile_section_app),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            ProfileMenuGroup(items = appItems)
        }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            TextButton(
                onClick = { showSignOutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(stringResource(R.string.sign_out), color = ErrorRed, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = NavySurface,
            title = { Text(stringResource(R.string.profile_signout_dialog_title), color = White) },
            text = {
                Text(
                    stringResource(R.string.profile_signout_dialog_message),
                    color = WhiteAlpha60
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text(stringResource(R.string.sign_out), color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.cancel), color = WhiteAlpha60)
                }
            }
        )
    }
}

@Composable
private fun ProfileAvatar(profile: UserProfile) {
    val name = profile.name?.trim()?.takeIf { it.isNotEmpty() }
        ?: stringResource(R.string.profile_name_placeholder)
    val initial = name.take(1).uppercase()
    val goalLabel = profile.mainGoal?.let {
        stringResource(
            when (it) {
                MainGoal.LOSE_WEIGHT -> R.string.profile_setup_main_goal_lose
                MainGoal.MAINTAIN_WEIGHT -> R.string.profile_setup_main_goal_maintain
                MainGoal.GAIN_WEIGHT -> R.string.profile_setup_main_goal_gain
            }
        )
    }
    val activityLabel = profile.activityLevel?.let {
        stringResource(
            when (it) {
                ActivityLevel.SEDENTARY -> R.string.profile_setup_activity_sedentary
                ActivityLevel.LIGHTLY_ACTIVE -> R.string.profile_setup_activity_lightly
                ActivityLevel.MODERATELY_ACTIVE -> R.string.profile_setup_activity_moderately
                ActivityLevel.VERY_ACTIVE -> R.string.profile_setup_activity_very
            }
        )
    }
    val subtitle = listOfNotNull(goalLabel, activityLabel).joinToString(" · ")
    val stepGoalLine = profile.dailyStepGoal?.let {
        stringResource(
            R.string.profile_daily_step_goal,
            ProfileDisplay.stepGoalText(it),
            stringResource(R.string.steps_unit)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(initial, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
        }
        Text(name, style = MaterialTheme.typography.headlineSmall)
        if (subtitle.isNotEmpty()) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
        }
        if (stepGoalLine != null) {
            Text(stepGoalLine, style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
        }
    }
}

@Composable
private fun ProfileStatsRow(profile: UserProfile) {
    val imperial = profile.units == MeasurementUnits.IMPERIAL

    val ageValue = profile.age?.toString()
    val ageChip = ageValue?.let {
        StatChipData(stringResource(R.string.profile_stat_age), it, "")
    }

    val heightChip = profile.heightCm?.let { cm ->
        if (imperial) {
            val (feet, inches) = UnitConversion.cmToFeetInches(cm)
            val ft = stringResource(R.string.profile_setup_height_hint_ft)
            val inch = stringResource(R.string.profile_setup_height_hint_in)
            StatChipData(stringResource(R.string.profile_stat_height), "$feet $ft $inches $inch", "")
        } else {
            StatChipData(stringResource(R.string.profile_stat_height), ProfileDisplay.formatDecimal(cm), stringResource(R.string.unit_cm))
        }
    }

    val weightChip = profile.currentWeightKg?.let { kg ->
        StatChipData(
            stringResource(R.string.profile_stat_weight),
            ProfileDisplay.weightValue(kg, imperial),
            stringResource(if (imperial) R.string.unit_lb else R.string.kg)
        )
    }

    val chips = listOfNotNull(ageChip, heightChip, weightChip)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chips.forEach { chip ->
            StatChip(
                label = chip.label,
                value = chip.value,
                unit = chip.unit,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class StatChipData(val label: String, val value: String, val unit: String)

private data class ProfileMenuItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit = {}
)

@Composable
private fun ProfileMenuGroup(items: List<ProfileMenuItemData>) {
    NovaCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        cornerRadius = 18.dp
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = item.onClick)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GreenPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = WhiteAlpha30,
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (index < items.size - 1) {
                    HorizontalDivider(color = NavyBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }
    }
}
