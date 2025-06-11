package dev.adriele.adolescare.chatbot

enum class MessageType(val type: Int) {
    RIGHT(1),
    LEFT(0),
    TYPING(2)
}