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
    private val _isPaused = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            combine(
                combine(
                    settingsRepo.serverIpFlow,
                    settingsRepo.serverPortFlow,
                    settingsRepo.targetFpsFlow,
                    ::Triple
                ),
                combine(
                    settingsRepo.cameraFacingFlow,
                    streamingClient.connectionState,
                    _isPaused,
                    ::Triple
                )
            ) { (ip, port, fps), (facing, connState, paused) ->
                StateUpdate(ip, port, fps, facing, connState, paused)
            }.collect { update ->
                _uiState.value = _uiState.value.copy(
                    endpoint = "${update.ip}:${update.port}",
                    targetFps = update.fps,
                    connectionStatus = update.connState.name,
                    isFrontCamera = update.facing == "Front",
                    isPaused = update.paused,
                    serverIp = update.ip,
                    serverPort = update.port
                )
            }
        }
    }

    private data class StateUpdate(
        val ip: String, 
        val port: String, 
        val fps: Int, 
        val facing: String,
        val connState: ConnectionState,
        val paused: Boolean
    )

    val frameAnalyzer = FrameAnalyzer { jpegBytes, width, height, rotation ->
        if (isStreaming && !_isPaused.value) {
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
        _isPaused.value = false
        viewModelScope.launch {
            val ip = settingsRepo.serverIpFlow.first() 
            val portStr = settingsRepo.serverPortFlow.first()
            val port = portStr.toIntOrNull() ?: 5000
            streamingClient.connect(if (ip.isBlank()) "192.168.1.100" else ip, port)
        }
    }

    fun stopStreaming() {
        if (!isStreaming) return
        isStreaming = false
        _isPaused.value = false
        viewModelScope.launch {
            streamingClient.disconnect()
        }
    }

    fun togglePause() {
        if (isStreaming) {
            _isPaused.value = !_isPaused.value
        }
    }

    fun switchCamera() {
        viewModelScope.launch {
            val currentFacing = settingsRepo.cameraFacingFlow.first()
            val newFacing = if (currentFacing == "Front") "Rear" else "Front"
            settingsRepo.updateCameraFacing(newFacing)
        }
    }

    fun updateServerIp(ip: String) {
        viewModelScope.launch { settingsRepo.updateServerIp(ip) }
    }

    fun updateServerPort(port: String) {
        viewModelScope.launch { settingsRepo.updateServerPort(port) }
    }

    fun updateTargetFps(fps: Int) {
        viewModelScope.launch { settingsRepo.updateTargetFps(fps) }
    }
}
