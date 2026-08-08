package com.example.robo_app.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.robo_app.data.model.StreamState
import com.example.robo_app.data.network.ConnectionState
import com.example.robo_app.data.network.FrameAnalyzer
import com.example.robo_app.data.network.StreamingClient
import com.example.robo_app.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RobotVisionViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = SettingsRepository(application)
    private val streamingClient = StreamingClient()
    
    private val _uiState = MutableStateFlow(StreamState())
    val uiState: StateFlow<StreamState> = _uiState.asStateFlow()
    
    private var isStreaming = false

    init {
        viewModelScope.launch {
            combine(
                settingsRepo.serverIpFlow,
                settingsRepo.serverPortFlow,
                settingsRepo.targetFpsFlow,
                streamingClient.connectionState
            ) { ip, port, fps, connState ->
                StateUpdate(ip, port, fps, connState)
            }.collect { update ->
                _uiState.value = _uiState.value.copy(
                    endpoint = "${update.ip}:${update.port}",
                    targetFps = update.fps,
                    connectionStatus = update.connState.name
                )
            }
        }
    }

    private data class StateUpdate(val ip: String, val port: String, val fps: Int, val connState: ConnectionState)

    val frameAnalyzer = FrameAnalyzer { jpegBytes, width, height, rotation ->
        if (isStreaming) {
            viewModelScope.launch {
                streamingClient.sendFrame(jpegBytes, width, height, rotation)
                // Update FPS and other telemetry heuristically here
                _uiState.value = _uiState.value.copy(
                    previewWidth = width,
                    previewHeight = height,
                    rotationDegrees = rotation,
                    fps = _uiState.value.targetFps.toString() // Mocked for now
                )
            }
        }
    }

    fun startStreaming() {
        if (isStreaming) return
        isStreaming = true
        viewModelScope.launch {
            val ip = settingsRepo.serverIpFlow.first() // Normally we'd use first() but keeping simple
            val port = 5000 // In reality parse from settings
            streamingClient.connect("192.168.1.100", 5000)
        }
    }

    fun stopStreaming() {
        if (!isStreaming) return
        isStreaming = false
        viewModelScope.launch {
            streamingClient.disconnect()
        }
    }
}
