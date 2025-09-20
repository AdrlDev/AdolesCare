package dev.adriele.adolescare.api.websocket

import android.util.Log
import dev.adriele.adolescare.api.websocket.contracts.IWebSocket
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor

class WebSocketClient(
    private val endpoint: String,
    private val callback: IWebSocket
) {

    private var webSocket: WebSocket? = null

    private var isConnected = false

    fun connect() {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val request = Request.Builder()
            .url("ws://72.60.193.190/ws/${endpoint.trim()}")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WEBSOCKET", "✅ Connected to $endpoint")
                isConnected = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WEBSOCKET", "📩 Message: $text")
                callback.onWebSocketResult(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d("WEBSOCKET", "❌ Closing: $code / $reason")
                webSocket.close(code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                Log.e("WEBSOCKET", "❗ Error: ${t.message}")
                callback.onWebSocketError("WebSocket error: ${t.message}")
                scheduleReconnect()
            }
        })
    }

    fun sendMessage(payload: String) {
        if(isConnected) {
            webSocket?.send(payload)
        } else {
            Log.w("WEBSOCKET", "⚠️ Cannot send, not connected yet.")
        }
    }

    fun close() {
        webSocket?.close(1000, "Manually closed")
        isConnected = false
    }

    private var retryCount = 0

    private fun scheduleReconnect() {
        val delay = (1000L * 5.coerceAtMost(retryCount)).coerceAtLeast(1000L) // max 5s
        retryCount++
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            connect()
        }, delay)
    }

    fun ping() {
        if (isConnected) {
            webSocket?.send("ping")
        }
    }

}

