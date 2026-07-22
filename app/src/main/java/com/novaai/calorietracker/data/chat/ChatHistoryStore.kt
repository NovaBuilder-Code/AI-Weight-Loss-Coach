package com.novaai.calorietracker.data.chat

import android.content.Context

/**
 * Local persistence for Nova chat history, backed by Room.
 * Follows the same Context-taking store style as the other data stores.
 * Not yet wired to the ChatScreen; messages never leave the device.
 */
object ChatHistoryStore {

    private fun dao(context: Context) =
        NovaChatDatabase.getInstance(context).chatMessageDao()

    /** Persists one message and returns its generated id. */
    suspend fun saveMessage(context: Context, message: ChatMessageEntity): Long =
        dao(context).insert(message)

    /** All saved messages, oldest first. */
    suspend fun getAllMessages(context: Context): List<ChatMessageEntity> =
        dao(context).getAll()

    /** Deletes the entire saved chat history. */
    suspend fun clearAll(context: Context) =
        dao(context).clearAll()
}
