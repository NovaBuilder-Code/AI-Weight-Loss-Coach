package com.novaai.calorietracker.ui.screens.calories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

private data class MealEntry(val name: String, val type: String, val kcal: Int)

private const val DAILY_GOAL = 2000
private const val BURNED = 340

@Composable
fun CalorieTrackerScreen(navController: NavController) {
    val typeBreakfast = stringResource(R.string.calorie_type_breakfast)
    val typeLunch = stringResource(R.string.calorie_type_lunch)
    val typeSnack = stringResource(R.string.calorie_type_snack)
    val meal1 = stringResource(R.string.calorie_meal1)
    val meal2 = stringResource(R.string.calorie_meal2)
    val meal3 = stringResource(R.string.calorie_meal3)

    val meals = remember {
        mutableStateListOf(
            MealEntry(meal1, typeBreakfast, 320),
            MealEntry(meal2, typeLunch, 650),
            MealEntry(meal3, typeSnack, 450)
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    val consumed = meals.sumOf { it.kcal }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            NovaTopBar(
                title = stringResource(R.string.placeholder_calorie_tracker_title),
                onBack = { navController.popBackStack() }
            ) {
                CircleIconButton(
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(R.string.calorie_add_meal_cd),
                    onClick = { showAddDialog = true }
                )
            }
        }
        item { CalorieSummary(consumed) }
        item { Spacer(Modifier.height(20.dp)) }
        item {
            SectionHeader(
                title = stringResource(R.string.calorie_section_meals),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        items(meals.size) { i ->
            val meal = meals[meals.size - 1 - i]
            MealRow(name = meal.name, type = meal.type, kcal = meal.kcal)
        }
    }

    if (showAddDialog) {
        AddMealDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, kcal ->
                meals.add(MealEntry(name, typeSnack, kcal))
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun CalorieSummary(consumed: Int) {
    val progress = (consumed.toFloat() / DAILY_GOAL).coerceIn(0f, 1f)
    NovaGlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_calories_today),
                style = MaterialTheme.typography.bodyMedium,
                color = WhiteAlpha60
            )
            Spacer(Modifier.height(16.dp))
            RingProgress(
                progress = progress,
                modifier = Modifier.size(150.dp),
                strokeWidth = 14f,
                progressColor = if (consumed > DAILY_GOAL) WarningAmber else GreenPrimary,
                trackColor = NavyBorder
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$consumed",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = stringResource(R.string.home_goal_format, DAILY_GOAL),
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteAlpha60
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    label = stringResource(R.string.home_remaining),
                    value = "${(DAILY_GOAL - consumed).coerceAtLeast(0)}",
                    unit = stringResource(R.string.kcal),
                    modifier = Modifier.weight(1f),
                    accentColor = GreenPrimary
                )
                StatChip(
                    label = stringResource(R.string.home_burned),
                    value = "$BURNED",
                    unit = stringResource(R.string.kcal),
                    modifier = Modifier.weight(1f),
                    accentColor = WarningAmber
                )
                StatChip(
                    label = stringResource(R.string.home_net),
                    value = "${consumed - BURNED}",
                    unit = stringResource(R.string.kcal),
                    modifier = Modifier.weight(1f),
                    accentColor = InfoBlue
                )
            }
        }
    }
}

@Composable
private fun MealRow(name: String, type: String, kcal: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NavyElevated)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GreenPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RestaurantMenu,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(type, style = MaterialTheme.typography.bodySmall, color = WhiteAlpha60)
        }
        Text(
            text = "$kcal ${stringResource(R.string.kcal)}",
            style = MaterialTheme.typography.titleMedium,
            color = GreenPrimary
        )
    }
}

@Composable
private fun AddMealDialog(onDismiss: () -> Unit, onAdd: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var kcalText by remember { mutableStateOf("") }
    val kcal = kcalText.toIntOrNull()
    val valid = name.isNotBlank() && kcal != null && kcal in 1..5000

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text(stringResource(R.string.calorie_dialog_title), color = White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.calorie_dialog_name), color = WhiteAlpha60) },
                    singleLine = true,
                    colors = novaFieldColors()
                )
                OutlinedTextField(
                    value = kcalText,
                    onValueChange = { kcalText = it },
                    label = { Text(stringResource(R.string.calorie_dialog_kcal), color = WhiteAlpha60) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = novaFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (valid) onAdd(name.trim(), kcal!!) },
                enabled = valid
            ) {
                Text(stringResource(R.string.calorie_add_button), color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = WhiteAlpha60) }
        }
    )
}

@Composable
private fun novaFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GreenPrimary,
    unfocusedBorderColor = NavyBorder,
    focusedTextColor = White,
    unfocusedTextColor = White,
    cursorColor = GreenPrimary,
    focusedContainerColor = NavyElevated,
    unfocusedContainerColor = NavyElevated
)
