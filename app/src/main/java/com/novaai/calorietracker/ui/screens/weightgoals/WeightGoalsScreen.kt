package com.novaai.calorietracker.ui.screens.weightgoals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

private const val START_WEIGHT = 78.0f
private const val GOAL_WEIGHT = 65.0f

@Composable
fun WeightGoalsScreen(navController: NavController) {
    var currentWeight by remember { mutableFloatStateOf(74.2f) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    val progressKg = ((currentWeight - START_WEIGHT) * 10).toInt() / 10f
    val progressFraction = ((START_WEIGHT - currentWeight) / (START_WEIGHT - GOAL_WEIGHT)).coerceIn(0f, 1f)

    Scaffold(
        containerColor = NavyDeep
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDeep)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            NovaTopBar(
                title = stringResource(R.string.weight_goals_title),
                onBack = { navController.popBackStack() }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                NovaAvatar(size = 72.dp)
            }

            NovaGlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatChip(
                        label = stringResource(R.string.weight_goals_current),
                        value = "$currentWeight",
                        unit = stringResource(R.string.kg),
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = White
                    )
                    StatChip(
                        label = stringResource(R.string.weight_goals_goal),
                        value = "65",
                        unit = stringResource(R.string.kg),
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = GreenPrimary
                    )
                    StatChip(
                        label = stringResource(R.string.weight_goals_progress),
                        value = "$progressKg",
                        unit = stringResource(R.string.kg),
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = InfoBlue
                    )
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GreenPrimary,
                        trackColor = NavyBorder
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            NovaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.weight_goals_progress_section),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.weight_goals_chart_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteAlpha60
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            NovaPrimaryButton(
                text = stringResource(R.string.weight_goals_update_button),
                onClick = { showUpdateDialog = true },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showUpdateDialog) {
        UpdateWeightDialog(
            onDismiss = { showUpdateDialog = false },
            onSave = { kg ->
                currentWeight = (kg * 10).toInt() / 10f
                showUpdateDialog = false
            }
        )
    }
}

@Composable
private fun UpdateWeightDialog(onDismiss: () -> Unit, onSave: (Float) -> Unit) {
    var text by remember { mutableStateOf("") }
    val parsed = text.replace(',', '.').toFloatOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text(stringResource(R.string.weight_dialog_title), color = White) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.weight_dialog_label), color = WhiteAlpha60) },
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
                enabled = parsed != null && parsed in 20f..300f
            ) {
                Text(stringResource(R.string.save), color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = WhiteAlpha60) }
        }
    )
}
