package dev.adriele.adolescare.database.repositories.implementation

import dev.adriele.adolescare.chatbot.ResponseType
import dev.adriele.adolescare.database.dao.ConversationDao
import dev.adriele.adolescare.database.entities.Conversations
import dev.adriele.adolescare.database.repositories.ChatBotRepository
import kotlinx.coroutines.flow.Flow

class ChatBotRepositoryImpl(private val conversationDao: ConversationDao) : ChatBotRepository {
    override suspend fun insertConversations(conversations: Conversations) {
        conversationDao.insertConversation(conversations)
    }

    override suspend fun deleteConversations(resType: ResponseType) {
        conversationDao.deleteConversation(resType)
    }

    override fun getAllConversations(userId: String): Flow<List<Conversations>> {
        return conversationDao.getAllConversations(userId)
    }

    override suspend fun clearMessages(userId: String) {
        conversationDao.clearConversations(userId)
    }
}