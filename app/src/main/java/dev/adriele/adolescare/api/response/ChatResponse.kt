package dev.adriele.adolescare.api.response

data class OuterResponse(
    val answer: ChatResponse
)

data class ChatResponse(
    val query: String,
    val result: String
)
