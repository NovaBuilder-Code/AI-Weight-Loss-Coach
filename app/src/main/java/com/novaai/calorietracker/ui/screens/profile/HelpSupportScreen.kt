package com.novaai.calorietracker.ui.screens.profile

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.*
import com.novaai.calorietracker.ui.theme.*

private val faqItems = listOf(
    R.string.help_faq1_q to R.string.help_faq1_a,
    R.string.help_faq2_q to R.string.help_faq2_a,
    R.string.help_faq3_q to R.string.help_faq3_a,
    R.string.help_faq4_q to R.string.help_faq4_a
)

@Composable
fun HelpSupportScreen(navController: NavController) {
    var expandedIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
            .verticalScroll(rememberScrollState())
    ) {
        NovaTopBar(
            title = stringResource(R.string.placeholder_help_title),
            onBack = { navController.popBackStack() }
        )

        SectionHeader(
            title = stringResource(R.string.help_section_faq),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        NovaCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            cornerRadius = 18.dp
        ) {
            Column(modifier = Modifier.animateContentSize()) {
                faqItems.forEachIndexed { index, (questionRes, answerRes) ->
                    val expanded = expandedIndex == index
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedIndex = if (expanded) -1 else index }
                            .padding(vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(questionRes),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = GreenPrimary
                            )
                        }
                        if (expanded) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(answerRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = WhiteAlpha60
                            )
                        }
                    }
                    if (index < faqItems.size - 1) {
                        HorizontalDivider(color = NavyBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        SectionHeader(
            title = stringResource(R.string.help_section_contact),
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.help_email_label),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.help_email),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenPrimary
                    )
                    Text(
                        text = stringResource(R.string.help_response),
                        style = MaterialTheme.typography.bodySmall,
                        color = WhiteAlpha60
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
