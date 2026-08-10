package com.example.robo_app.data.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class FrameMetadata(
    val frameId: Long,
    val timestamp: Long,
    val width: Int,
    val height: Int,
    val rotation: Int
)

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

class StreamingClient {
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 20_000
        }
    }

    private var session: DefaultClientWebSocketSession? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private var frameCounter = 0L

    suspend fun connect(ip: String, port: Int) {
        _connectionState.value = ConnectionState.CONNECTING
        try {
            session = client.webSocketSession(host = ip, port = port, path = "/stream")
            _connectionState.value = ConnectionState.CONNECTED
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = ConnectionState.ERROR
        }
    }

    suspend fun sendFrame(jpegBytes: ByteArray, width: Int, height: Int, rotation: Int) {
        if (_connectionState.value != ConnectionState.CONNECTED || session == null) return

        try {
            val metadata = FrameMetadata(
                frameId = ++frameCounter,
                timestamp = System.currentTimeMillis(),
                width = width,
                height = height,
                rotation = rotation
            )

            // Send metadata as text frame
            session?.send(Frame.Text(Json.encodeToString(metadata)))
            
            // Send JPEG bytes as binary frame
            session?.send(Frame.Binary(true, jpegBytes))
            
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = ConnectionState.ERROR
            disconnect()
        }
    }

    suspend fun disconnect() {
        session?.close(CloseReason(CloseReason.Codes.NORMAL, "Client disconnected"))
        session = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
}
