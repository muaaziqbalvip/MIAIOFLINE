package com.miai.offline.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.miai.offline.data.model.ChatMessage
import com.miai.offline.data.model.Conversation

@Database(
    entities = [ChatMessage::class, Conversation::class],
    version = 1,
    exportSchema = false
)
abstract class MiAiDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: MiAiDatabase? = null

        fun getInstance(context: Context): MiAiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MiAiDatabase::class.java,
                    "miai_offline.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
