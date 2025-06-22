package dev.adriele.adolescare.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.adriele.adolescare.api.response.Sources
import dev.adriele.adolescare.chatbot.ResponseType

@Entity(tableName = "chat_bot_conversation")
data class Conversations(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String? = null,
    val resWith: ResponseType? = null,
    val message: String? = null,
    val receivedDate: String? = null,
    val sentDate: String? = null,
    val sources: List<Sources>? = null
)
