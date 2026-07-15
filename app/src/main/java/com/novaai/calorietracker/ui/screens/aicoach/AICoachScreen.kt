package com.novaai.calorietracker.ui.screens.aicoach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.ui.components.NovaAvatar
import com.novaai.calorietracker.ui.components.NovaTopBar
import com.novaai.calorietracker.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class CoachMessage(val id: Int, val text: String, val isFromUser: Boolean)

@Composable
fun AICoachScreen(navController: NavController) {
    val welcomeMessage = stringResource(R.string.ai_coach_welcome_message)
    val aiResponses = listOf(
        stringResource(R.string.chat_ai_response_1),
        stringResource(R.string.chat_ai_response_2),
        stringResource(R.string.chat_ai_response_3),
        stringResource(R.string.chat_ai_response_4),
        stringResource(R.string.chat_ai_response_5)
    )

    var messages by remember(welcomeMessage) {
        mutableStateOf(listOf(CoachMessage(0, welcomeMessage, false)))
    }
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var messageIdCounter by remember { mutableIntStateOf(1) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isBlank()) return
        messages = messages + CoachMessage(messageIdCounter++, text, true)
        inputText = ""
        isTyping = true
        scope.launch {
            listState.animateScrollToItem(messages.size)
            delay(1200)
            messages = messages + CoachMessage(messageIdCounter++, aiResponses.random(), false)
            isTyping = false
            delay(100)
            listState.animateScrollToItem(messages.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        NovaTopBar(
            title = stringResource(R.string.ai_coach_title),
            onBack = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NovaAvatar(size = 80.dp)
        }

        // Chat area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                CoachMessageBubble(msg)
            }
            if (isTyping) {
                item { CoachTypingBubble() }
            }
        }

        // Input bar
        Surface(color = NavySurface, tonalElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.ai_coach_input_hint),
                            color = WhiteAlpha30,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavyElevated,
                        unfocusedContainerColor = NavyElevated,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        cursorColor = GreenPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                    singleLine = true
                )
                IconButton(
                    onClick = { sendMessage() },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) GreenPrimary else NavyElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = stringResource(R.string.chat_send_cd),
                        tint = if (inputText.isNotBlank()) NavyDeep else WhiteAlpha30
                    )
                }
            }
        }
    }
}

@Composable
private fun CoachMessageBubble(message: CoachMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isFromUser) {
            NovaAvatar(size = 32.dp)
            Spacer(Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (message.isFromUser) 20.dp else 4.dp,
                        bottomEnd = if (message.isFromUser) 4.dp else 20.dp
                    )
                )
                .background(
                    if (message.isFromUser)
                        Brush.linearGradient(listOf(GreenDim, GreenPrimary))
                    else
                        Brush.linearGradient(listOf(NavyElevated, NavySurface))
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.isFromUser) NavyDeep else White
            )
        }
        if (message.isFromUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NavyElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = WhiteAlpha60, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun CoachTypingBubble() {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NovaAvatar(size = 32.dp)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                .background(NavyElevated)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("…", color = WhiteAlpha60, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
