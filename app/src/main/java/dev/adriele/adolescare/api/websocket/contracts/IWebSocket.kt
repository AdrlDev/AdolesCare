package dev.adriele.adolescare.api.websocket.contracts

interface IWebSocket {
    fun onWebSocketResult(message: String)
    fun onWebSocketError(error: String)
}