package com.novaai.calorietracker.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.novaai.calorietracker.R
import com.novaai.calorietracker.data.ChatResult
import com.novaai.calorietracker.data.NovaChatService
import com.novaai.calorietracker.data.chat.ChatHistoryStore
import com.novaai.calorietracker.data.chat.ChatMessageEntity
import com.novaai.calorietracker.data.chat.ChatSender
import com.novaai.calorietracker.navigation.Screen
import com.novaai.calorietracker.ui.components.NovaAvatar
import com.novaai.calorietracker.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Data ──────────────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: Int,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String = ""
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ChatScreen(navController: NavController) {
    val initialMessage = stringResource(R.string.chat_initial_message)
    val initialMessages = remember(initialMessage) {
        listOf(ChatMessage(0, initialMessage, false, "Now"))
    }

    val quickReplies = listOf(
        stringResource(R.string.chat_quick_dinner),
        stringResource(R.string.chat_quick_on_track),
        stringResource(R.string.chat_quick_workout),
        stringResource(R.string.chat_quick_protein),
        stringResource(R.string.chat_quick_calories)
    )

    val errorTimeout = stringResource(R.string.chat_error_timeout)
    val errorNetwork = stringResource(R.string.chat_error_network)
    val errorServer = stringResource(R.string.chat_error_server)

    var messages by remember { mutableStateOf(initialMessages) }
    var inputText by remember { mutableStateOf("") }
    var pendingReplies by remember { mutableIntStateOf(0) }
    val isTyping = pendingReplies > 0
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var messageIdCounter by remember { mutableIntStateOf(1) }
    val context = LocalContext.current

    // Restore saved history on open. The welcome message is display-only
    // (never persisted), so it appears exactly once above the saved thread.
    LaunchedEffect(Unit) {
        val saved = ChatHistoryStore.getAllMessages(context)
        if (saved.isNotEmpty()) {
            messages = initialMessages + saved.map { entity ->
                ChatMessage(messageIdCounter++, entity.text, entity.sender == ChatSender.USER)
            }
            listState.scrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(messageIdCounter++, text.trim(), true, "Now")
        messages = messages + userMsg
        inputText = ""
        pendingReplies++

        scope.launch {
            ChatHistoryStore.saveMessage(
                context,
                ChatMessageEntity(text = userMsg.text, sender = ChatSender.USER, timestamp = System.currentTimeMillis())
            )
            listState.animateScrollToItem(messages.size)
            // Send through the Cloudflare Worker backend (which holds the
            // OpenAI key server-side — no keys ship inside the app).
            val result = NovaChatService.sendMessage(userMsg.text)
            val responseText = when (result) {
                is ChatResult.Success -> result.reply
                ChatResult.Timeout -> errorTimeout
                ChatResult.NetworkError -> errorNetwork
                ChatResult.ServerError -> errorServer
            }
            // Only real Nova replies become history; error bubbles stay display-only.
            if (result is ChatResult.Success) {
                ChatHistoryStore.saveMessage(
                    context,
                    ChatMessageEntity(text = result.reply, sender = ChatSender.NOVA, timestamp = System.currentTimeMillis())
                )
            }
            messages = messages + ChatMessage(messageIdCounter++, responseText, false, "Now")
            pendingReplies--
            delay(100)
            listState.animateScrollToItem(messages.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        ChatHeader(
            navController = navController,
            onClearChat = {
                messages = initialMessages
            }
        )

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically { it / 2 } + fadeIn()
                ) {
                    MessageBubble(msg)
                }
            }
            if (isTyping) {
                item { TypingIndicator() }
            }
        }

        // Quick replies
        AnimatedVisibility(visible = messages.size <= 2) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickReplies) { reply ->
                    QuickReplyChip(reply) { sendMessage(reply) }
                }
            }
        }

        // Input bar
        ChatInputBar(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = { sendMessage(inputText) }
        )
    }
}

@Composable
private fun ChatHeader(navController: NavController, onClearChat: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(NavySurface, NavyDeep))
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NovaAvatar(size = 52.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text("Nova AI", style = MaterialTheme.typography.titleLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary)
                )
                Text(stringResource(R.string.chat_status), style = MaterialTheme.typography.labelSmall, color = WhiteAlpha60)
            }
        }
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.chat_options_cd), tint = WhiteAlpha60)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = NavySurface
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_menu_settings), color = White) },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = GreenPrimary) },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(Screen.Settings.route)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_menu_profile), color = White) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GreenPrimary) },
                    onClick = {
                        menuExpanded = false
                        navController.navigate(Screen.Profile.route)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.chat_menu_clear), color = White) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = WhiteAlpha60) },
                    onClick = {
                        menuExpanded = false
                        onClearChat()
                    }
                )
            }
        }
    }
    HorizontalDivider(color = NavyBorder, thickness = 1.dp)
}

@Composable
private fun MessageBubble(message: ChatMessage) {
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
private fun TypingIndicator() {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NovaAvatar(size = 32.dp)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
                .background(NavyElevated)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            BouncingDots()
        }
    }
}

@Composable
private fun BouncingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        (0..2).forEach { index ->
            val offset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 900
                        0f at (index * 150)
                        -6f at (index * 150 + 200)
                        0f at (index * 150 + 400)
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = offset.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary)
            )
        }
    }
}

@Composable
private fun QuickReplyChip(text: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.labelMedium, color = White) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = NavyElevated,
            selectedContainerColor = GreenPrimary.copy(alpha = 0.2f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = NavyBorder
        )
    )
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
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
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(stringResource(R.string.chat_input_hint), color = WhiteAlpha30, style = MaterialTheme.typography.bodyMedium)
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
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                singleLine = true
            )
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (text.isNotBlank()) GreenPrimary else NavyElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = stringResource(R.string.chat_send_cd),
                    tint = if (text.isNotBlank()) NavyDeep else WhiteAlpha30
                )
            }
        }
    }
}
