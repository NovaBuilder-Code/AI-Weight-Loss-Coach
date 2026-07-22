package com.novaai.calorietracker.data.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatMessageDao {

    @Insert
    suspend fun insert(message: ChatMessageEntity): Long

    /** All messages in chronological order; id breaks ties for equal timestamps. */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC, id ASC")
    suspend fun getAll(): List<ChatMessageEntity>

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()
}
