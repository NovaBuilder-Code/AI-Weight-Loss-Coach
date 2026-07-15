package com.novaai.calorietracker.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

@Composable
fun PrivacyScreen(navController: NavController) {
    var analytics by remember { mutableStateOf(false) }
    var personalization by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(
            title = stringResource(R.string.placeholder_privacy_title),
            onBack = { navController.popBackStack() }
        )

        SectionHeader(
            title = stringResource(R.string.privacy_section_controls),
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
                NovaToggleRow(
                    icon = Icons.Default.Insights,
                    title = stringResource(R.string.privacy_analytics),
                    subtitle = stringResource(R.string.privacy_analytics_sub),
                    checked = analytics,
                    onCheckedChange = { analytics = it },
                    accentColor = InfoBlue
                )
                NovaToggleRow(
                    icon = Icons.Default.Psychology,
                    title = stringResource(R.string.privacy_personal),
                    subtitle = stringResource(R.string.privacy_personal_sub),
                    checked = personalization,
                    onCheckedChange = { personalization = it }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        SectionHeader(
            title = stringResource(R.string.privacy_section_data),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        NovaCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            cornerRadius = 18.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.privacy_local_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = GreenPrimary
                    )
                    Text(
                        text = stringResource(R.string.privacy_local_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteAlpha60
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
