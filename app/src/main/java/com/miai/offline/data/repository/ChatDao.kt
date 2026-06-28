package com.miai.offline.data.repository

import androidx.room.*
import com.miai.offline.data.model.ChatMessage
import com.miai.offline.data.model.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>>

    @Insert
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("SELECT * FROM conversations ORDER BY lastUpdated DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: Conversation)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
}
