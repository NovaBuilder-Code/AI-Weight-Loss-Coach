package com.novaai.calorietracker.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.ActivityLevel
import com.novaai.calorietracker.data.MainGoal
import com.novaai.calorietracker.data.MeasurementUnits
import com.novaai.calorietracker.data.ProfileDisplay
import com.novaai.calorietracker.data.ProfileValidation
import com.novaai.calorietracker.data.Sex
import com.novaai.calorietracker.data.UnitConversion
import com.novaai.calorietracker.data.UserProfileStore
import com.novaai.calorietracker.ui.components.NovaPrimaryButton
import com.novaai.calorietracker.ui.components.NovaTopBar
import com.novaai.calorietracker.ui.components.SelectOptionCard
import com.novaai.calorietracker.ui.components.SectionHeader
import com.novaai.calorietracker.ui.theme.*
import com.novaai.calorietracker.ui.screens.onboarding.DEFAULT_STEP_GOAL
import com.novaai.calorietracker.ui.screens.onboarding.MAX_AGE
import com.novaai.calorietracker.ui.screens.onboarding.MAX_HEIGHT_CM
import com.novaai.calorietracker.ui.screens.onboarding.MAX_STEP_GOAL
import com.novaai.calorietracker.ui.screens.onboarding.MAX_WEIGHT_KG
import com.novaai.calorietracker.ui.screens.onboarding.MIN_AGE
import com.novaai.calorietracker.ui.screens.onboarding.MIN_STEP_GOAL

private fun parseDecimal(text: String): Float? =
    text.trim().replace(',', '.').toFloatOrNull()

@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val saved = remember { UserProfileStore.load(context) }
    val imperial = saved.units == MeasurementUnits.IMPERIAL

    var name by rememberSaveable { mutableStateOf(saved.name ?: "") }
    var ageText by rememberSaveable { mutableStateOf(saved.age?.toString() ?: "") }
    var sexName by rememberSaveable { mutableStateOf(saved.sex?.name ?: "") }

    var heightCmText by rememberSaveable {
        mutableStateOf(if (!imperial) saved.heightCm?.let { ProfileDisplay.formatDecimal(it) } ?: "" else "")
    }
    var heightFtText by rememberSaveable {
        mutableStateOf(if (imperial) saved.heightCm?.let { UnitConversion.cmToFeetInches(it).first.toString() } ?: "" else "")
    }
    var heightInText by rememberSaveable {
        mutableStateOf(if (imperial) saved.heightCm?.let { UnitConversion.cmToFeetInches(it).second.toString() } ?: "" else "")
    }
    var weightText by rememberSaveable {
        mutableStateOf(saved.currentWeightKg?.let { ProfileDisplay.weightValue(it, imperial) } ?: "")
    }
    var goalWeightText by rememberSaveable {
        mutableStateOf(saved.goalWeightKg?.let { ProfileDisplay.weightValue(it, imperial) } ?: "")
    }
    var mainGoalName by rememberSaveable { mutableStateOf(saved.mainGoal?.name ?: "") }
    var activityLevelName by rememberSaveable { mutableStateOf(saved.activityLevel?.name ?: "") }
    var stepGoalText by rememberSaveable {
        mutableStateOf((saved.dailyStepGoal ?: DEFAULT_STEP_GOAL).toString())
    }

    val age = ageText.trim().toIntOrNull()
    val ageValid = age != null && ProfileValidation.validAge(age)
    val sex = Sex.entries.firstOrNull { it.name == sexName }
    val mainGoal = MainGoal.entries.firstOrNull { it.name == mainGoalName }
    val activityLevel = ActivityLevel.entries.firstOrNull { it.name == activityLevelName }
    val stepGoal = stepGoalText.trim().toIntOrNull()
    val stepGoalValid = stepGoal != null && ProfileValidation.validStepGoal(stepGoal)

    val heightCm: Float? = if (imperial) {
        val ft = heightFtText.trim().toIntOrNull()
        val inch = if (heightInText.isBlank()) 0f else parseDecimal(heightInText)
        if (ft == null || inch == null || inch < 0f || inch >= 12f) null
        else UnitConversion.feetInchesToCm(ft, inch)
    } else {
        parseDecimal(heightCmText)
    }
    val heightValid = heightCm != null && ProfileValidation.validHeightCm(heightCm)

    fun weightKgOf(text: String): Float? =
        parseDecimal(text)?.let { if (imperial) UnitConversion.lbToKg(it) else it }

    val weightKg = weightKgOf(weightText)
    val weightValid = weightKg != null && ProfileValidation.validWeightKg(weightKg)
    val goalWeightKg = weightKgOf(goalWeightText)
    val goalWeightValid = goalWeightKg != null && ProfileValidation.validWeightKg(goalWeightKg)

    val formValid = name.isNotBlank() && ageValid && sex != null && heightValid &&
        weightValid && goalWeightValid && mainGoal != null && activityLevel != null && stepGoalValid

    fun saveAndFinish() {
        val current = UserProfileStore.load(context)
        UserProfileStore.save(
            context,
            current.copy(
                name = name.trim(),
                age = age,
                sex = sex,
                heightCm = heightCm,
                currentWeightKg = weightKg,
                goalWeightKg = goalWeightKg,
                mainGoal = mainGoal,
                activityLevel = activityLevel,
                dailyStepGoal = stepGoal
            )
        )
        navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .imePadding()
    ) {
        NovaTopBar(
            title = stringResource(R.string.profile_edit_title),
            onBack = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            SectionHeader(
                title = stringResource(R.string.profile_edit_section_personal),
                modifier = Modifier.padding(horizontal = 0.dp)
            )
            Spacer(Modifier.height(10.dp))

            EditTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.pi_name),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            EditTextField(
                value = ageText,
                onValueChange = { input -> ageText = input.filter { it.isDigit() }.take(3) },
                label = stringResource(R.string.pi_age),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            EditError(
                visible = ageText.isNotEmpty() && !ageValid,
                text = stringResource(R.string.profile_setup_age_invalid, MIN_AGE, MAX_AGE)
            )
            Spacer(Modifier.height(8.dp))
            EditSectionLabel(stringResource(R.string.profile_edit_label_sex))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

            Spacer(Modifier.height(20.dp))
            SectionHeader(
                title = stringResource(R.string.profile_edit_section_body),
                modifier = Modifier.padding(horizontal = 0.dp)
            )
            Spacer(Modifier.height(10.dp))

            EditSectionLabel(stringResource(R.string.profile_edit_label_height))
            if (imperial) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EditTextField(
                        value = heightFtText,
                        onValueChange = { input -> heightFtText = input.filter { it.isDigit() }.take(1) },
                        label = stringResource(R.string.profile_setup_height_hint_ft),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    EditTextField(
                        value = heightInText,
                        onValueChange = { input -> heightInText = input.filter { it.isDigit() }.take(2) },
                        label = stringResource(R.string.profile_setup_height_hint_in),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                EditTextField(
                    value = heightCmText,
                    onValueChange = { input ->
                        heightCmText = input.filter { it.isDigit() || it == '.' || it == ',' }.take(5)
                    },
                    label = stringResource(R.string.profile_setup_height_hint_cm),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            EditError(
                visible = (if (imperial) heightFtText.isNotEmpty() || heightInText.isNotEmpty() else heightCmText.isNotEmpty()) && !heightValid,
                text = stringResource(R.string.profile_setup_height_invalid)
            )
            EditTextField(
                value = weightText,
                onValueChange = { input ->
                    weightText = input.filter { it.isDigit() || it == '.' || it == ',' }.take(6)
                },
                label = stringResource(
                    if (imperial) R.string.profile_setup_weight_hint_lb
                    else R.string.profile_setup_weight_hint_kg
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            EditError(
                visible = weightText.isNotEmpty() && !weightValid,
                text = stringResource(R.string.profile_setup_weight_invalid)
            )
            EditTextField(
                value = goalWeightText,
                onValueChange = { input ->
                    goalWeightText = input.filter { it.isDigit() || it == '.' || it == ',' }.take(6)
                },
                label = stringResource(
                    if (imperial) R.string.profile_setup_weight_hint_lb
                    else R.string.profile_setup_weight_hint_kg
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            EditError(
                visible = goalWeightText.isNotEmpty() && !goalWeightValid,
                text = stringResource(R.string.profile_setup_weight_invalid)
            )

            Spacer(Modifier.height(20.dp))
            SectionHeader(
                title = stringResource(R.string.profile_edit_section_goals),
                modifier = Modifier.padding(horizontal = 0.dp)
            )
            Spacer(Modifier.height(10.dp))

            EditSectionLabel(stringResource(R.string.profile_edit_label_main_goal))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SelectOptionCard(
                    icon = Icons.Default.TrendingDown,
                    label = stringResource(R.string.profile_setup_main_goal_lose),
                    selected = mainGoal == MainGoal.LOSE_WEIGHT,
                    onClick = { mainGoalName = MainGoal.LOSE_WEIGHT.name }
                )
                SelectOptionCard(
                    icon = Icons.Default.TrendingFlat,
                    label = stringResource(R.string.profile_setup_main_goal_maintain),
                    selected = mainGoal == MainGoal.MAINTAIN_WEIGHT,
                    onClick = { mainGoalName = MainGoal.MAINTAIN_WEIGHT.name }
                )
                SelectOptionCard(
                    icon = Icons.Default.TrendingUp,
                    label = stringResource(R.string.profile_setup_main_goal_gain),
                    selected = mainGoal == MainGoal.GAIN_WEIGHT,
                    onClick = { mainGoalName = MainGoal.GAIN_WEIGHT.name }
                )
            }

            Spacer(Modifier.height(12.dp))
            EditSectionLabel(stringResource(R.string.profile_edit_label_activity))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SelectOptionCard(
                    icon = Icons.Default.Chair,
                    label = stringResource(R.string.profile_setup_activity_sedentary),
                    subtitle = stringResource(R.string.profile_setup_activity_sedentary_sub),
                    selected = activityLevel == ActivityLevel.SEDENTARY,
                    onClick = { activityLevelName = ActivityLevel.SEDENTARY.name }
                )
                SelectOptionCard(
                    icon = Icons.Default.DirectionsWalk,
                    label = stringResource(R.string.profile_setup_activity_lightly),
                    subtitle = stringResource(R.string.profile_setup_activity_lightly_sub),
                    selected = activityLevel == ActivityLevel.LIGHTLY_ACTIVE,
                    onClick = { activityLevelName = ActivityLevel.LIGHTLY_ACTIVE.name }
                )
                SelectOptionCard(
                    icon = Icons.Default.DirectionsRun,
                    label = stringResource(R.string.profile_setup_activity_moderately),
                    subtitle = stringResource(R.string.profile_setup_activity_moderately_sub),
                    selected = activityLevel == ActivityLevel.MODERATELY_ACTIVE,
                    onClick = { activityLevelName = ActivityLevel.MODERATELY_ACTIVE.name }
                )
                SelectOptionCard(
                    icon = Icons.Default.Bolt,
                    label = stringResource(R.string.profile_setup_activity_very),
                    subtitle = stringResource(R.string.profile_setup_activity_very_sub),
                    selected = activityLevel == ActivityLevel.VERY_ACTIVE,
                    onClick = { activityLevelName = ActivityLevel.VERY_ACTIVE.name }
                )
            }

            Spacer(Modifier.height(12.dp))
            EditSectionLabel(stringResource(R.string.profile_edit_label_step_goal))
            EditTextField(
                value = stepGoalText,
                onValueChange = { input -> stepGoalText = input.filter { it.isDigit() }.take(6) },
                label = stringResource(R.string.profile_setup_step_goal_hint),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            EditError(
                visible = stepGoalText.isNotEmpty() && !stepGoalValid,
                text = stringResource(R.string.profile_setup_step_goal_invalid, MIN_STEP_GOAL, MAX_STEP_GOAL)
            )

            Spacer(Modifier.height(24.dp))
        }

        NovaPrimaryButton(
            text = stringResource(R.string.save),
            onClick = { saveAndFinish() },
            enabled = formValid,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun EditSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = WhiteAlpha60,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = WhiteAlpha60) },
        modifier = modifier.fillMaxWidth(),
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
private fun EditError(visible: Boolean, text: String) {
    if (visible) {
        Spacer(Modifier.height(6.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = ErrorRed)
    }
}