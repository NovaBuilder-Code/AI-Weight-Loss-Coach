package com.novaai.calorietracker.ui.screens.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

private val SleepPurple = Color(0xFF9B8FFF)
private const val GOAL_HOURS = 8f

private data class SleepNight(val day: String, val hours: Float)

@Composable
fun SleepTrackerScreen(navController: NavController) {
    val week = remember {
        mutableStateListOf(
            SleepNight("Mon", 6.8f),
            SleepNight("Tue", 7.5f),
            SleepNight("Wed", 8.1f),
            SleepNight("Thu", 6.2f),
            SleepNight("Fri", 7.9f),
            SleepNight("Sat", 8.4f),
            SleepNight("Sun", 7.3f)
        )
    }
    var showLogDialog by remember { mutableStateOf(false) }
    val lastNight = week.last().hours
    val hoursLabel = "${lastNight.toInt()}h ${((lastNight % 1) * 60).toInt()}m"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(
            title = stringResource(R.string.placeholder_sleep_title),
            onBack = { navController.popBackStack() }
        )

        NovaGlowCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.sleep_last_night),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhiteAlpha60
                )
                RingProgress(
                    progress = (lastNight / GOAL_HOURS).coerceIn(0f, 1f),
                    modifier = Modifier.size(170.dp),
                    strokeWidth = 15f,
                    progressColor = SleepPurple,
                    trackColor = NavyBorder
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = SleepPurple,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = hoursLabel,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = stringResource(R.string.sleep_quality) + ": " +
                                stringResource(R.string.sleep_quality_good),
                            style = MaterialTheme.typography.labelSmall,
                            color = WhiteAlpha60
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SleepTimeChip(
                        icon = Icons.Default.Bedtime,
                        label = stringResource(R.string.sleep_bedtime),
                        value = "23:10",
                        modifier = Modifier.weight(1f)
                    )
                    SleepTimeChip(
                        icon = Icons.Default.WbSunny,
                        label = stringResource(R.string.sleep_wakeup),
                        value = "06:30",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        SectionHeader(
            title = stringResource(R.string.sleep_section_week),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        NovaCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                week.forEach { night ->
                    SleepBar(night)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        NovaCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            gradient = Brush.linearGradient(listOf(SleepPurple.copy(alpha = 0.10f), NavyElevated))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.sleep_tip_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = SleepPurple
                )
                Text(
                    text = stringResource(R.string.sleep_tip_text),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        NovaPrimaryButton(
            text = stringResource(R.string.sleep_log_button),
            onClick = { showLogDialog = true },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(24.dp))
    }

    if (showLogDialog) {
        LogSleepDialog(
            onDismiss = { showLogDialog = false },
            onSave = { hours ->
                week[week.lastIndex] = week.last().copy(hours = hours)
                showLogDialog = false
            }
        )
    }
}

@Composable
private fun SleepTimeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(NavyElevated)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = SleepPurple, modifier = Modifier.size(18.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = WhiteAlpha60)
        }
    }
}

@Composable
private fun SleepBar(night: SleepNight) {
    val fraction = (night.hours / 10f).coerceIn(0f, 1f)
    val reachedGoal = night.hours >= GOAL_HOURS
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "%.1f".format(night.hours),
            style = MaterialTheme.typography.labelSmall,
            color = WhiteAlpha60
        )
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(90.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .background(NavyElevated)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (reachedGoal) GreenPrimary else SleepPurple,
                                (if (reachedGoal) GreenPrimary else SleepPurple).copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }
        Text(
            text = night.day,
            style = MaterialTheme.typography.labelSmall,
            color = WhiteAlpha60
        )
    }
}

@Composable
private fun LogSleepDialog(onDismiss: () -> Unit, onSave: (Float) -> Unit) {
    var text by remember { mutableStateOf("") }
    val parsed = text.replace(',', '.').toFloatOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text(stringResource(R.string.sleep_dialog_title), color = White) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.sleep_dialog_label), color = WhiteAlpha60) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                onClick = { parsed?.let { onSave(it) } },
                enabled = parsed != null && parsed in 0f..24f
            ) {
                Text(stringResource(R.string.save), color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = WhiteAlpha60) }
        }
    )
}
