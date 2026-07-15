package com.novaai.calorietracker.ui.screens.water

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

private val WaterBlue = Color(0xFF40CFFF)
private const val GOAL_ML = 2500

@Composable
fun WaterTrackerScreen(navController: NavController) {
    var intakeMl by remember { mutableIntStateOf(1800) }
    var lastAdded by remember { mutableIntStateOf(0) }

    val liters = intakeMl / 1000f
    val progress = (intakeMl.toFloat() / GOAL_ML).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(
            title = stringResource(R.string.placeholder_water_title),
            onBack = { navController.popBackStack() }
        )

        NovaGlowCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.water_today),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhiteAlpha60
                )
                RingProgress(
                    progress = progress,
                    modifier = Modifier.size(170.dp),
                    strokeWidth = 15f,
                    progressColor = WaterBlue,
                    trackColor = NavyBorder
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = WaterBlue,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "%.1f".format(liters),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = stringResource(R.string.liters),
                            style = MaterialTheme.typography.bodySmall,
                            color = WhiteAlpha60
                        )
                    }
                }
                Text(
                    text = if (intakeMl >= GOAL_ML)
                        stringResource(R.string.water_goal_reached)
                    else
                        stringResource(R.string.water_goal_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (intakeMl >= GOAL_ML) GreenPrimary else WhiteAlpha60
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    intakeMl += 250
                    lastAdded = 250
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WaterBlue.copy(alpha = 0.15f),
                    contentColor = WaterBlue
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.water_add_glass), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    intakeMl += 500
                    lastAdded = 500
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WaterBlue,
                    contentColor = NavyDeep
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(stringResource(R.string.water_add_bottle), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                if (lastAdded > 0) {
                    intakeMl = (intakeMl - lastAdded).coerceAtLeast(0)
                    lastAdded = 0
                }
            },
            enabled = lastAdded > 0,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = WhiteAlpha60),
            border = BorderStroke(1.dp, NavyBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
        ) {
            Text(stringResource(R.string.water_undo))
        }

        Spacer(Modifier.height(20.dp))

        NovaCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            gradient = Brush.linearGradient(listOf(WaterBlue.copy(alpha = 0.10f), NavyElevated))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.water_tip_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = WaterBlue
                )
                Text(
                    text = stringResource(R.string.water_tip_text),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
