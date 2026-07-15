package com.novaai.calorietracker.ui.screens.meals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

private data class MealIdea(
    val emoji: String,
    val titleRes: Int,
    val subRes: Int,
    val kcal: Int,
    val tagRes: Int
)

private val mealIdeas = listOf(
    MealIdea("🐟", R.string.meals_item1_title, R.string.meals_item1_sub, 520, R.string.meals_tag_protein),
    MealIdea("🥦", R.string.meals_item2_title, R.string.meals_item2_sub, 450, R.string.meals_tag_balanced),
    MealIdea("🥣", R.string.meals_item3_title, R.string.meals_item3_sub, 320, R.string.meals_tag_light),
    MealIdea("🥗", R.string.meals_item4_title, R.string.meals_item4_sub, 480, R.string.meals_tag_veggie)
)

@Composable
fun MealSuggestionsScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            NovaTopBar(
                title = stringResource(R.string.placeholder_meal_suggestions_title),
                onBack = { navController.popBackStack() }
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NovaAvatar(size = 44.dp)
                Text(
                    text = stringResource(R.string.meals_header),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhiteAlpha60
                )
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
        items(mealIdeas.size) { i ->
            MealIdeaCard(mealIdeas[i])
        }
    }
}

@Composable
private fun MealIdeaCard(idea: MealIdea) {
    NovaCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        cornerRadius = 18.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GreenPrimary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text(idea.emoji, fontSize = 26.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(idea.titleRes), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(idea.subRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = WhiteAlpha60
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GreenPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(idea.tagRes),
                        color = GreenPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${idea.kcal}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GreenPrimary
                )
                Text(
                    text = stringResource(R.string.kcal),
                    style = MaterialTheme.typography.labelSmall,
                    color = WhiteAlpha60
                )
            }
        }
    }
}
