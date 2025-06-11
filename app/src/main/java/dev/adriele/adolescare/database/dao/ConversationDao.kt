package dev.adriele.adolescare.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.adriele.adolescare.chatbot.ResponseType
import dev.adriele.adolescare.database.entities.Conversations
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversation(conversation: Conversations)

    @Query("DELETE FROM chat_bot_conversation WHERE resWith = :resType")
    suspend fun deleteConversation(resType: ResponseType)

    @Query("SELECT * FROM chat_bot_conversation WHERE userId =:userId ORDER BY id ASC")
    fun getAllConversations(userId: String): Flow<List<Conversations>>

    @Query("DELETE FROM chat_bot_conversation WHERE userId =:userId")
    suspend fun clearConversations(userId: String)
}