package com.novaai.calorietracker.ui.screens.streaks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*
import kotlinx.coroutines.launch

private const val CURRENT_STREAK = 12
private const val BEST_STREAK = 35

private data class StreakDay(val label: String, val completed: Boolean)

@Composable
fun DailyStreaksScreen(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val completedMessage = stringResource(R.string.streaks_completed_snackbar)

    var todayCompleted by remember { mutableStateOf(false) }
    var currentStreak by remember { mutableIntStateOf(CURRENT_STREAK) }

    val week = listOf(
        StreakDay(stringResource(R.string.streaks_day_mon), true),
        StreakDay(stringResource(R.string.streaks_day_tue), true),
        StreakDay(stringResource(R.string.streaks_day_wed), true),
        StreakDay(stringResource(R.string.streaks_day_thu), false),
        StreakDay(stringResource(R.string.streaks_day_fri), false),
        StreakDay(stringResource(R.string.streaks_day_sat), false),
        StreakDay(stringResource(R.string.streaks_day_sun), todayCompleted)
    )

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
                title = stringResource(R.string.placeholder_streaks_title),
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "$currentStreak",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = stringResource(R.string.streaks_days_unit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhiteAlpha60
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatChip(
                            label = stringResource(R.string.streaks_current_streak),
                            value = "$currentStreak",
                            unit = stringResource(R.string.streaks_days_unit),
                            modifier = Modifier.weight(1f),
                            accentColor = WarningAmber
                        )
                        StatChip(
                            label = stringResource(R.string.streaks_best_streak),
                            value = "$BEST_STREAK",
                            unit = stringResource(R.string.streaks_days_unit),
                            modifier = Modifier.weight(1f),
                            accentColor = GreenPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            NovaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.streaks_weekly_progress),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        week.forEach { day -> WeekDayIndicator(day) }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            NovaCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                gradient = Brush.linearGradient(listOf(WarningAmber.copy(alpha = 0.12f), NavyElevated))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.streaks_motivation_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = WarningAmber
                    )
                    Text(
                        text = stringResource(R.string.streaks_motivation_text),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            NovaPrimaryButton(
                text = if (todayCompleted)
                    stringResource(R.string.streaks_completed_button)
                else
                    stringResource(R.string.streaks_complete_button),
                onClick = {
                    if (!todayCompleted) {
                        todayCompleted = true
                        currentStreak += 1
                        scope.launch { snackbarHostState.showSnackbar(completedMessage) }
                    }
                },
                enabled = !todayCompleted,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WeekDayIndicator(day: StreakDay) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (day.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (day.completed) GreenPrimary else WhiteAlpha30,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = day.label,
            style = MaterialTheme.typography.labelSmall,
            color = WhiteAlpha60
        )
    }
}
