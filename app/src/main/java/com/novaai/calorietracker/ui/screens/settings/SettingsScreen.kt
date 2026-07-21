package com.novaai.calorietracker.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.AppLanguage
import com.novaai.calorietracker.data.LanguageStore
import com.novaai.calorietracker.data.ThemeMode
import com.novaai.calorietracker.data.ThemeStore
import com.novaai.calorietracker.ui.components.NovaCard
import com.novaai.calorietracker.ui.components.NovaTopBar
import com.novaai.calorietracker.ui.components.SectionHeader
import com.novaai.calorietracker.ui.theme.GreenPrimary
import com.novaai.calorietracker.ui.theme.NavyDeep
import com.novaai.calorietracker.ui.theme.NovaThemeState
import com.novaai.calorietracker.ui.theme.WhiteAlpha30

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        NovaTopBar(
            title = stringResource(R.string.placeholder_settings_title),
            onBack = { navController.popBackStack() }
        )

        SectionHeader(
            title = stringResource(R.string.settings_section_appearance),
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
                ThemeModeRow(R.string.settings_theme_system, ThemeMode.SYSTEM)
                ThemeModeRow(R.string.settings_theme_dark, ThemeMode.DARK)
                ThemeModeRow(R.string.settings_theme_light, ThemeMode.LIGHT)
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionHeader(
            title = stringResource(R.string.settings_section_language),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))
        val context = LocalContext.current
        var selectedLanguage by remember { mutableStateOf(LanguageStore.load(context)) }
        NovaCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            cornerRadius = 18.dp
        ) {
            Column {
                LanguageRow(R.string.settings_language_english, AppLanguage.ENGLISH, selectedLanguage) {
                    selectedLanguage = it
                    LanguageStore.save(context, it)
                }
                LanguageRow(R.string.settings_language_norsk, AppLanguage.NORSK, selectedLanguage) {
                    selectedLanguage = it
                    LanguageStore.save(context, it)
                }
                LanguageRow(R.string.settings_language_svenska, AppLanguage.SVENSKA, selectedLanguage) {
                    selectedLanguage = it
                    LanguageStore.save(context, it)
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(
    @StringRes labelRes: Int,
    language: AppLanguage,
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(language) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected == language,
            onClick = { onSelect(language) },
            colors = RadioButtonDefaults.colors(
                selectedColor = GreenPrimary,
                unselectedColor = WhiteAlpha30
            )
        )
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ThemeModeRow(@StringRes labelRes: Int, mode: ThemeMode) {
    val context = LocalContext.current
    val select = {
        NovaThemeState.mode = mode
        ThemeStore.save(context, mode)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = select)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = NovaThemeState.mode == mode,
            onClick = select,
            colors = RadioButtonDefaults.colors(
                selectedColor = GreenPrimary,
                unselectedColor = WhiteAlpha30
            )
        )
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleMedium
        )
    }
}
