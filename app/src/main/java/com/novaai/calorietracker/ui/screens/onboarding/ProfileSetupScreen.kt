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
import com.novaai.calorietracker.data.Sex
import com.novaai.calorietracker.data.UserProfileStore
import com.novaai.calorietracker.navigation.Screen
import com.novaai.calorietracker.ui.components.NovaPrimaryButton
import com.novaai.calorietracker.ui.theme.*

private const val STEP_COUNT = 3
const val MIN_AGE = 13
const val MAX_AGE = 120

/**
 * First onboarding slice: name, age and sex, one question per step.
 * Each answer is merged into UserProfileStore when the user continues,
 * so values survive process death and moving back and forth.
 */
@Composable
fun ProfileSetupScreen(navController: NavController) {
    val context = LocalContext.current
    val saved = remember { UserProfileStore.load(context) }

    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf(saved.name ?: "") }
    var ageText by rememberSaveable { mutableStateOf(saved.age?.toString() ?: "") }
    var sexName by rememberSaveable { mutableStateOf(saved.sex?.name ?: "") }

    val trimmedName = name.trim()
    val age = ageText.trim().toIntOrNull()
    val ageValid = age != null && age in MIN_AGE..MAX_AGE
    val sex = Sex.entries.firstOrNull { it.name == sexName }

    val stepValid = when (step) {
        0 -> trimmedName.isNotEmpty()
        1 -> ageValid
        else -> sex != null
    }

    fun goBack() {
        if (step > 0) step-- else navController.popBackStack()
    }

    fun saveAndContinue() {
        val current = UserProfileStore.load(context)
        when (step) {
            0 -> UserProfileStore.save(context, current.copy(name = trimmedName))
            1 -> UserProfileStore.save(context, current.copy(age = age))
            2 -> {
                UserProfileStore.save(context, current.copy(sex = sex))
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
            else -> QuestionHeader(
                title = stringResource(R.string.profile_setup_sex_title),
                subtitle = stringResource(R.string.profile_setup_sex_sub)
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
                if (ageText.isNotEmpty() && !ageValid) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.profile_setup_age_invalid, MIN_AGE, MAX_AGE),
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed
                    )
                }
            }
            else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SexOptionCard(
                    icon = Icons.Default.Male,
                    label = stringResource(R.string.profile_setup_sex_male),
                    selected = sex == Sex.MALE,
                    onClick = { sexName = Sex.MALE.name }
                )
                SexOptionCard(
                    icon = Icons.Default.Female,
                    label = stringResource(R.string.profile_setup_sex_female),
                    selected = sex == Sex.FEMALE,
                    onClick = { sexName = Sex.FEMALE.name }
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
private fun SexOptionCard(
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
