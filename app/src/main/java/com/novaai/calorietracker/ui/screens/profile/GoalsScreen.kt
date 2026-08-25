package com.novaai.calorietracker.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.MeasurementUnits
import com.novaai.calorietracker.data.ProfileGoals
import com.novaai.calorietracker.data.SleepGoalStore
import com.novaai.calorietracker.data.UserProfileStore
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.screens.onboarding.MAX_STEP_GOAL
import com.novaai.calorietracker.ui.screens.onboarding.MIN_STEP_GOAL
import com.novaai.calorietracker.ui.theme.*

private data class GoalItem(
    val id: String,
    val icon: ImageVector,
    val labelRes: Int,
    val color: Color,
    val isDecimal: Boolean,
    val editable: Boolean = true,
    val allowZero: Boolean = false,
    val maxValue: Float = 100000f
)

@Composable
fun GoalsScreen(navController: NavController) {
    val context = LocalContext.current
    val profile = remember { UserProfileStore.load(context) }
    val imperial = profile.units == MeasurementUnits.IMPERIAL
    val goals = remember(imperial) {
        listOf(
            GoalItem("calories", Icons.Default.LocalFireDepartment, R.string.goals_calories, WarningAmber, false, editable = false),
            GoalItem("steps",    Icons.Default.DirectionsWalk,      R.string.goals_steps,    GreenPrimary, false),
            GoalItem("water",    Icons.Default.WaterDrop,           R.string.goals_water,    Color(0xFF40CFFF), true),
            GoalItem("sleep",    Icons.Default.NightsStay,          R.string.goals_sleep,    Color(0xFF9B8FFF), true, allowZero = true, maxValue = ProfileGoals.MAX_SLEEP_GOAL),
            GoalItem("weight",   Icons.Default.MonitorWeight,       if (imperial) R.string.goals_weight_imperial else R.string.goals_weight, InfoBlue, true)
        )
    }
    val values = remember(profile) {
        mutableStateMapOf(
            "calories" to ProfileGoals.calorieTarget(profile).toString(),
            "steps" to ProfileGoals.stepGoal(profile).toString(),
            "water" to "2.5",
            "sleep" to ProfileGoals.formatSleepGoal(SleepGoalStore.load(context)),
            "weight" to ProfileGoals.goalWeightText(profile)
        )
    }
    var editing by remember { mutableStateOf<GoalItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(
            title = stringResource(R.string.placeholder_goals_title),
            onBack = { navController.popBackStack() }
        )

        Text(
            text = stringResource(R.string.goals_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = WhiteAlpha60,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(16.dp))

        SectionHeader(
            title = stringResource(R.string.goals_section),
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
                goals.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (item.editable) Modifier.clickable { editing = item } else Modifier)
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(item.color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = stringResource(item.labelRes),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = values[item.id] ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = item.color
                        )
                        if (item.editable) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.goals_dialog_title),
                                tint = WhiteAlpha30,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (index < goals.size - 1) {
                        HorizontalDivider(color = NavyBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    editing?.let { item ->
        EditGoalDialog(
            item = item,
            currentValue = values[item.id] ?: "",
            onDismiss = { editing = null },
            onSave = { newValue ->
                values[item.id] = newValue
                when (item.id) {
                    "steps" -> {
                        val steps = newValue.trim().toIntOrNull()
                        if (steps != null && steps in MIN_STEP_GOAL..MAX_STEP_GOAL) {
                            UserProfileStore.save(
                                context,
                                UserProfileStore.load(context).copy(dailyStepGoal = steps)
                            )
                        }
                    }
                    "weight" -> {
                        ProfileGoals.parseWeightToKg(newValue, profile.units == MeasurementUnits.IMPERIAL)
                            ?.let { kg ->
                                UserProfileStore.save(
                                    context,
                                    UserProfileStore.load(context).copy(goalWeightKg = kg)
                                )
                            }
                    }
                    "sleep" -> {
                        val hours = normalizedSleep(newValue)
                        if (hours != null && ProfileGoals.validSleepGoal(hours)) {
                            SleepGoalStore.save(context, hours)
                        }
                    }
                    else -> Unit
                }
                editing = null
            }
        )
    }
}

/** Normalizes "0"/"0.0"/",5" etc. to a float; null when not a number. */
private fun normalizedSleep(text: String): Float? = text.trim().replace(',', '.').toFloatOrNull()

@Composable
private fun EditGoalDialog(
    item: GoalItem,
    currentValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentValue) }
    val normalized = text.replace(',', '.')
    val valid = if (item.isDecimal) {
        val v = normalized.toFloatOrNull()
        v != null && v >= 0f && (v > 0f || item.allowZero) && v < item.maxValue
    } else {
        text.toIntOrNull()?.let { it in 1..100000 } == true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text(stringResource(item.labelRes), color = White) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.goals_dialog_label), color = WhiteAlpha60) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (item.isDecimal) KeyboardType.Decimal else KeyboardType.Number
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = NavyBorder,
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    cursorColor = GreenPrimary,
                    focusedContainerColor = NavyElevated,
                    unfocusedContainerColor = NavyElevated
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (valid) onSave(if (item.isDecimal) normalized else text.trim()) },
                enabled = valid
            ) {
                Text(stringResource(R.string.save), color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = WhiteAlpha60) }
        }
    )
}
