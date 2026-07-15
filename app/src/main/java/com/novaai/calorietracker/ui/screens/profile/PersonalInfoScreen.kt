package com.novaai.calorietracker.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.novaai.calorietracker.ui.components.NovaPrimaryButton
import com.novaai.calorietracker.ui.components.NovaTopBar
import com.novaai.calorietracker.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PersonalInfoScreen(navController: NavController) {
    var name by remember { mutableStateOf("Alex Johnson") }
    var email by remember { mutableStateOf("alex@email.com") }
    var age by remember { mutableStateOf("29") }
    var height by remember { mutableStateOf("175") }
    var weight by remember { mutableStateOf("74.2") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.pi_saved)

    Scaffold(
        containerColor = NavyDeep,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDeep)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            NovaTopBar(
                title = stringResource(R.string.placeholder_personal_info_title),
                onBack = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "A",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PersonalInfoField(stringResource(R.string.pi_name), name, { name = it })
                PersonalInfoField(stringResource(R.string.pi_email), email, { email = it }, KeyboardType.Email)
                PersonalInfoField(stringResource(R.string.pi_age), age, { age = it }, KeyboardType.Number)
                PersonalInfoField(stringResource(R.string.pi_height), height, { height = it }, KeyboardType.Number)
                PersonalInfoField(stringResource(R.string.pi_weight), weight, { weight = it }, KeyboardType.Decimal)
            }

            Spacer(Modifier.height(8.dp))

            NovaPrimaryButton(
                text = stringResource(R.string.pi_save),
                onClick = {
                    scope.launch { snackbarHostState.showSnackbar(savedMessage) }
                },
                enabled = name.isNotBlank() && email.isNotBlank(),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PersonalInfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = WhiteAlpha60) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
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
}
