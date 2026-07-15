package com.novaai.calorietracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.novaai.calorietracker.ui.theme.*

@Composable
fun NovaCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    gradient: Brush? = null,
    showBorder: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (showBorder) Modifier.border(
                    width = 1.dp,
                    color = NavyBorder,
                    shape = shape
                ) else Modifier
            )
            .background(gradient ?: Brush.linearGradient(listOf(NavyElevated, NavySurface)))
            .padding(20.dp),
        content = content
    )
}

@Composable
fun NovaGlowCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    NovaCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        gradient = Brush.linearGradient(
            colors = listOf(
                GreenPrimary.copy(alpha = 0.12f),
                NavyElevated
            )
        ),
        showBorder = false,
        content = content
    )
}
