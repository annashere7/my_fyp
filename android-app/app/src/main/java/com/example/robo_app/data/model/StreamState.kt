package com.example.robo_app.data.model

data class StreamState(
    val previewWidth: Int = 1280,
    val previewHeight: Int = 720,
    val rotationDegrees: Int = 90,
    val fps: String = "0",
    val latencyMs: String = "0",
    val targetFps: Int = 20,
    val connectionStatus: String = "Disconnected",
    val endpoint: String = "N/A"
)
