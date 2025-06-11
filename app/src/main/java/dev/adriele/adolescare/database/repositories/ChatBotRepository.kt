package dev.adriele.adolescare.database.repositories

import dev.adriele.adolescare.chatbot.ResponseType
import dev.adriele.adolescare.database.entities.Conversations
import kotlinx.coroutines.flow.Flow

interface ChatBotRepository {
    suspend fun insertConversations(conversations: Conversations)

    suspend fun deleteConversations(resType: ResponseType)

    fun getAllConversations(userId: String): Flow<List<Conversations>>

    suspend fun clearMessages(userId: String)
}