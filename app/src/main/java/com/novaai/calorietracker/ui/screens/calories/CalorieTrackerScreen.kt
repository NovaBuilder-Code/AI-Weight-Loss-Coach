package com.novaai.calorietracker.ui.screens.calories

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

private data class MealEntry(val name: String, val type: String, val kcal: Int, val date: String)

private const val DAILY_GOAL = 2000
private const val BURNED = 340
private const val PREFS_NAME = "calorie_tracker"
private const val KEY_MEALS = "meals"

private fun loadMeals(prefs: SharedPreferences, type: String): List<MealEntry> =
    runCatching {
        val array = JSONArray(prefs.getString(KEY_MEALS, "[]").orEmpty())
        (0 until array.length()).mapNotNull { i ->
            val obj = array.optJSONObject(i) ?: return@mapNotNull null
            val name = obj.optString("name")
            val kcal = obj.optInt("kcal", -1)
            if (name.isBlank() || kcal < 0) null
            else MealEntry(name, type, kcal, obj.optString("date"))
        }
    }.getOrDefault(emptyList())

private fun saveMeals(prefs: SharedPreferences, meals: List<MealEntry>) {
    val array = JSONArray()
    meals.forEach { meal ->
        array.put(
            JSONObject()
                .put("name", meal.name)
                .put("kcal", meal.kcal)
                .put("date", meal.date)
        )
    }
    prefs.edit().putString(KEY_MEALS, array.toString()).apply()
}

@Composable
fun CalorieTrackerScreen(navController: NavController) {
    val typeSnack = stringResource(R.string.calorie_type_snack)
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    val meals = remember {
        mutableStateListOf<MealEntry>().apply {
            val today = LocalDate.now().toString()
            val saved = loadMeals(prefs, typeSnack)
            val todays = saved.filter { it.date == today }
            addAll(todays)
            // A new calendar day: drop the previous day's meals from storage.
            if (todays.size != saved.size) saveMeals(prefs, todays)
        }
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
            val index = meals.size - 1 - i
            val meal = meals[index]
            MealRow(
                name = meal.name,
                type = meal.type,
                kcal = meal.kcal,
                onDelete = {
                    meals.removeAt(index)
                    saveMeals(prefs, meals)
                }
            )
        }
    }

    if (showAddDialog) {
        AddMealDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, kcal ->
                meals.add(MealEntry(name, typeSnack, kcal, LocalDate.now().toString()))
                saveMeals(prefs, meals)
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
private fun MealRow(name: String, type: String, kcal: Int, onDelete: () -> Unit) {
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
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.calorie_delete_meal_cd),
                tint = WhiteAlpha60,
                modifier = Modifier.size(16.dp)
            )
        }
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
