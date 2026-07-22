package com.novaai.calorietracker.data.chat

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Who authored a chat message. */
enum class ChatSender { USER, NOVA }

/** One persisted Nova chat message. */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val sender: ChatSender,
    val timestamp: Long
)
