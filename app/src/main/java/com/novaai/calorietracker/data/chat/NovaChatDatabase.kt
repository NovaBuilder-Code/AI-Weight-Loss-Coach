package com.novaai.calorietracker.data.chat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class ChatConverters {
    @TypeConverter
    fun fromSender(sender: ChatSender): String = sender.name

    @TypeConverter
    fun toSender(name: String): ChatSender = ChatSender.valueOf(name)
}

@Database(entities = [ChatMessageEntity::class], version = 1, exportSchema = false)
@TypeConverters(ChatConverters::class)
abstract class NovaChatDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var instance: NovaChatDatabase? = null

        fun getInstance(context: Context): NovaChatDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NovaChatDatabase::class.java,
                    "nova_chat.db"
                ).build().also { instance = it }
            }
    }
}
