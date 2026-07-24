package com.novaai.calorietracker.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.MeasurementUnits
import com.novaai.calorietracker.data.Sex
import com.novaai.calorietracker.data.UnitConversion
import com.novaai.calorietracker.data.UserProfileStore
import com.novaai.calorietracker.navigation.Screen
import com.novaai.calorietracker.ui.components.NovaPrimaryButton
import com.novaai.calorietracker.ui.theme.*
import java.util.Locale

private const val STEP_COUNT = 7
const val MIN_AGE = 13
const val MAX_AGE = 120
const val MIN_HEIGHT_CM = 90f
const val MAX_HEIGHT_CM = 250f
const val MIN_WEIGHT_KG = 30f
const val MAX_WEIGHT_KG = 300f

private fun parseDecimal(text: String): Float? =
    text.trim().replace(',', '.').toFloatOrNull()

/** "175" or "74.2" — no trailing ".0", US decimal point (parse accepts both). */
private fun formatDecimal(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString()
    else String.format(Locale.US, "%.1f", value)

/**
 * Onboarding questionnaire: name, age, sex (12B1) plus units, height,
 * current weight and goal weight (12B2), one question per step. Each answer
 * is merged into UserProfileStore on Continue; height/weights are stored
 * canonically in metric and converted for imperial display and input.
 */
@Composable
fun ProfileSetupScreen(navController: NavController) {
    val context = LocalContext.current
    val saved = remember { UserProfileStore.load(context) }

    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf(saved.name ?: "") }
    var ageText by rememberSaveable { mutableStateOf(saved.age?.toString() ?: "") }
    var sexName by rememberSaveable { mutableStateOf(saved.sex?.name ?: "") }
    var unitsName by rememberSaveable { mutableStateOf(saved.units?.name ?: "") }

    val savedImperial = saved.units == MeasurementUnits.IMPERIAL
    var heightCmText by rememberSaveable {
        mutableStateOf(if (!savedImperial) saved.heightCm?.let { formatDecimal(it) } ?: "" else "")
    }
    var heightFtText by rememberSaveable {
        mutableStateOf(if (savedImperial) saved.heightCm?.let { UnitConversion.cmToFeetInches(it).first.toString() } ?: "" else "")
    }
    var heightInText by rememberSaveable {
        mutableStateOf(if (savedImperial) saved.heightCm?.let { UnitConversion.cmToFeetInches(it).second.toString() } ?: "" else "")
    }
    var weightText by rememberSaveable {
        mutableStateOf(saved.currentWeightKg?.let { formatDecimal(if (savedImperial) UnitConversion.kgToLb(it) else it) } ?: "")
    }
    var goalWeightText by rememberSaveable {
        mutableStateOf(saved.goalWeightKg?.let { formatDecimal(if (savedImperial) UnitConversion.kgToLb(it) else it) } ?: "")
    }

    val trimmedName = name.trim()
    val age = ageText.trim().toIntOrNull()
    val ageValid = age != null && age in MIN_AGE..MAX_AGE
    val sex = Sex.entries.firstOrNull { it.name == sexName }
    val units = MeasurementUnits.entries.firstOrNull { it.name == unitsName }
    val imperial = units == MeasurementUnits.IMPERIAL

    val heightCm: Float? = if (imperial) {
        val ft = heightFtText.trim().toIntOrNull()
        val inch = if (heightInText.isBlank()) 0f else parseDecimal(heightInText)
        if (ft == null || inch == null || inch < 0f || inch >= 12f) null
        else UnitConversion.feetInchesToCm(ft, inch)
    } else {
        parseDecimal(heightCmText)
    }
    val heightValid = heightCm != null && heightCm in MIN_HEIGHT_CM..MAX_HEIGHT_CM

    fun weightKgOf(text: String): Float? =
        parseDecimal(text)?.let { if (imperial) UnitConversion.lbToKg(it) else it }

    val weightKg = weightKgOf(weightText)
    val weightValid = weightKg != null && weightKg in MIN_WEIGHT_KG..MAX_WEIGHT_KG
    val goalWeightKg = weightKgOf(goalWeightText)
    val goalWeightValid = goalWeightKg != null && goalWeightKg in MIN_WEIGHT_KG..MAX_WEIGHT_KG

    /** Switching systems converts anything already typed so nothing is lost. */
    fun selectUnits(new: MeasurementUnits) {
        val old = units
        if (old == new) return
        if (old != null) {
            if (new == MeasurementUnits.IMPERIAL) {
                parseDecimal(heightCmText)?.let { cm ->
                    val (ft, inch) = UnitConversion.cmToFeetInches(cm)
                    heightFtText = ft.toString()
                    heightInText = inch.toString()
                }
                parseDecimal(weightText)?.let { weightText = formatDecimal(UnitConversion.kgToLb(it)) }
                parseDecimal(goalWeightText)?.let { goalWeightText = formatDecimal(UnitConversion.kgToLb(it)) }
            } else {
                val ft = heightFtText.trim().toIntOrNull()
                val inch = if (heightInText.isBlank()) 0f else parseDecimal(heightInText)
                if (ft != null && inch != null) {
                    heightCmText = formatDecimal(UnitConversion.feetInchesToCm(ft, inch))
                }
                parseDecimal(weightText)?.let { weightText = formatDecimal(UnitConversion.lbToKg(it)) }
                parseDecimal(goalWeightText)?.let { goalWeightText = formatDecimal(UnitConversion.lbToKg(it)) }
            }
        }
        unitsName = new.name
    }

    val stepValid = when (step) {
        0 -> trimmedName.isNotEmpty()
        1 -> ageValid
        2 -> sex != null
        3 -> units != null
        4 -> heightValid
        5 -> weightValid
        else -> goalWeightValid
    }

    fun goBack() {
        if (step > 0) step-- else navController.popBackStack()
    }

    fun saveAndContinue() {
        val current = UserProfileStore.load(context)
        when (step) {
            0 -> UserProfileStore.save(context, current.copy(name = trimmedName))
            1 -> UserProfileStore.save(context, current.copy(age = age))
            2 -> UserProfileStore.save(context, current.copy(sex = sex))
            3 -> UserProfileStore.save(context, current.copy(units = units))
            4 -> UserProfileStore.save(context, current.copy(heightCm = heightCm))
            5 -> UserProfileStore.save(context, current.copy(currentWeightKg = weightKg))
            6 -> {
                UserProfileStore.save(context, current.copy(goalWeightKg = goalWeightKg))
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
                return
            }
        }
        step++
    }

    BackHandler(enabled = step > 0) { step-- }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { goBack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = White
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.profile_setup_step, step + 1, STEP_COUNT),
                style = MaterialTheme.typography.labelLarge,
                color = WhiteAlpha60
            )
        }

        LinearProgressIndicator(
            progress = { (step + 1) / STEP_COUNT.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp)),
            color = GreenPrimary,
            trackColor = NavyElevated
        )

        Spacer(Modifier.height(32.dp))

        when (step) {
            0 -> QuestionHeader(
                title = stringResource(R.string.profile_setup_name_title),
                subtitle = stringResource(R.string.profile_setup_name_sub)
            )
            1 -> QuestionHeader(
                title = stringResource(R.string.profile_setup_age_title),
                subtitle = stringResource(R.string.profile_setup_age_sub)
            )
            2 -> QuestionHeader(
                title = stringResource(R.string.profile_setup_sex_title),
                subtitle = stringResource(R.string.profile_setup_sex_sub)
            )
            3 -> QuestionHeader(
                title = stringResource(R.string.profile_setup_units_title),
                subtitle = stringResource(R.string.profile_setup_units_sub)
            )
            4 -> QuestionHeader(
                title = stringResource(R.string.profile_setup_height_title),
                subtitle = stringResource(R.string.profile_setup_height_sub)
            )
            5 -> QuestionHeader(
                title = stringResource(R.string.profile_setup_weight_title),
                subtitle = stringResource(R.string.profile_setup_weight_sub)
            )
            else -> QuestionHeader(
                title = stringResource(R.string.profile_setup_goal_weight_title),
                subtitle = stringResource(R.string.profile_setup_goal_weight_sub)
            )
        }

        Spacer(Modifier.height(24.dp))

        when (step) {
            0 -> SetupTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.profile_setup_name_hint),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            1 -> {
                SetupTextField(
                    value = ageText,
                    onValueChange = { input -> ageText = input.filter { it.isDigit() }.take(3) },
                    placeholder = stringResource(R.string.profile_setup_age_hint),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                ValidationError(
                    visible = ageText.isNotEmpty() && !ageValid,
                    text = stringResource(R.string.profile_setup_age_invalid, MIN_AGE, MAX_AGE)
                )
            }
            2 -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SelectOptionCard(
                    icon = Icons.Default.Male,
                    label = stringResource(R.string.profile_setup_sex_male),
                    selected = sex == Sex.MALE,
                    onClick = { sexName = Sex.MALE.name }
                )
                SelectOptionCard(
                    icon = Icons.Default.Female,
                    label = stringResource(R.string.profile_setup_sex_female),
                    selected = sex == Sex.FEMALE,
                    onClick = { sexName = Sex.FEMALE.name }
                )
            }
            3 -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SelectOptionCard(
                    icon = Icons.Default.Straighten,
                    label = stringResource(R.string.profile_setup_units_metric),
                    selected = units == MeasurementUnits.METRIC,
                    onClick = { selectUnits(MeasurementUnits.METRIC) }
                )
                SelectOptionCard(
                    icon = Icons.Default.SquareFoot,
                    label = stringResource(R.string.profile_setup_units_imperial),
                    selected = units == MeasurementUnits.IMPERIAL,
                    onClick = { selectUnits(MeasurementUnits.IMPERIAL) }
                )
            }
            4 -> {
                if (imperial) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            SetupTextField(
                                value = heightFtText,
                                onValueChange = { input -> heightFtText = input.filter { it.isDigit() }.take(1) },
                                placeholder = stringResource(R.string.profile_setup_height_hint_ft),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            SetupTextField(
                                value = heightInText,
                                onValueChange = { input -> heightInText = input.filter { it.isDigit() }.take(2) },
                                placeholder = stringResource(R.string.profile_setup_height_hint_in),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                } else {
                    SetupTextField(
                        value = heightCmText,
                        onValueChange = { input ->
                            heightCmText = input.filter { it.isDigit() || it == '.' || it == ',' }.take(5)
                        },
                        placeholder = stringResource(R.string.profile_setup_height_hint_cm),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                ValidationError(
                    visible = (if (imperial) heightFtText.isNotEmpty() || heightInText.isNotEmpty() else heightCmText.isNotEmpty()) && !heightValid,
                    text = stringResource(R.string.profile_setup_height_invalid)
                )
            }
            5 -> {
                SetupTextField(
                    value = weightText,
                    onValueChange = { input ->
                        weightText = input.filter { it.isDigit() || it == '.' || it == ',' }.take(6)
                    },
                    placeholder = stringResource(
                        if (imperial) R.string.profile_setup_weight_hint_lb
                        else R.string.profile_setup_weight_hint_kg
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                ValidationError(
                    visible = weightText.isNotEmpty() && !weightValid,
                    text = stringResource(R.string.profile_setup_weight_invalid)
                )
            }
            else -> {
                SetupTextField(
                    value = goalWeightText,
                    onValueChange = { input ->
                        goalWeightText = input.filter { it.isDigit() || it == '.' || it == ',' }.take(6)
                    },
                    placeholder = stringResource(
                        if (imperial) R.string.profile_setup_weight_hint_lb
                        else R.string.profile_setup_weight_hint_kg
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                ValidationError(
                    visible = goalWeightText.isNotEmpty() && !goalWeightValid,
                    text = stringResource(R.string.profile_setup_weight_invalid)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        NovaPrimaryButton(
            text = stringResource(
                if (step == STEP_COUNT - 1) R.string.profile_setup_finish
                else R.string.profile_setup_continue
            ),
            onClick = { saveAndContinue() },
            enabled = stepValid
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun QuestionHeader(title: String, subtitle: String) {
    Text(text = title, style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(8.dp))
    Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = WhiteAlpha60)
}

@Composable
private fun ValidationError(visible: Boolean, text: String) {
    if (visible) {
        Spacer(Modifier.height(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = ErrorRed)
    }
}

@Composable
private fun SetupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = WhiteAlpha30) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = NavyElevated,
            unfocusedContainerColor = NavyElevated,
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = NavyBorder,
            focusedTextColor = White,
            unfocusedTextColor = White,
            cursorColor = GreenPrimary
        ),
        keyboardOptions = keyboardOptions,
        singleLine = true
    )
}

@Composable
private fun SelectOptionCard(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) GreenPrimary.copy(alpha = 0.12f) else NavyElevated)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) GreenPrimary else NavyBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) GreenPrimary else WhiteAlpha60
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) GreenPrimary else White
        )
        Spacer(Modifier.weight(1f))
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = GreenPrimary,
                unselectedColor = WhiteAlpha30
            )
        )
    }
}
