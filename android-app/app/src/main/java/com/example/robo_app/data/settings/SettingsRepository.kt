package com.example.robo_app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "robot_vision_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val SERVER_IP = stringPreferencesKey("server_ip")
        val SERVER_PORT = stringPreferencesKey("server_port")
        val TARGET_FPS = intPreferencesKey("target_fps")
        val RESOLUTION = stringPreferencesKey("resolution")
        val CAMERA_FACING = stringPreferencesKey("camera_facing")
        val ENABLE_TORCH = booleanPreferencesKey("enable_torch")
    }

    val serverIpFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_IP] ?: "192.168.1.100"
    }

    val serverPortFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_PORT] ?: "5000"
    }

    val targetFpsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[TARGET_FPS] ?: 20
    }

    val resolutionFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[RESOLUTION] ?: "1280x720"
    }

    val cameraFacingFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CAMERA_FACING] ?: "Rear"
    }

    val enableTorchFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ENABLE_TORCH] ?: false
    }

    suspend fun updateServerIp(ip: String) {
        context.dataStore.edit { preferences -> preferences[SERVER_IP] = ip }
    }

    suspend fun updateServerPort(port: String) {
        context.dataStore.edit { preferences -> preferences[SERVER_PORT] = port }
    }

    suspend fun updateTargetFps(fps: Int) {
        context.dataStore.edit { preferences -> preferences[TARGET_FPS] = fps }
    }

    suspend fun updateResolution(res: String) {
        context.dataStore.edit { preferences -> preferences[RESOLUTION] = res }
    }

    suspend fun updateCameraFacing(facing: String) {
        context.dataStore.edit { preferences -> preferences[CAMERA_FACING] = facing }
    }

    suspend fun updateEnableTorch(enable: Boolean) {
        context.dataStore.edit { preferences -> preferences[ENABLE_TORCH] = enable }
    }
}
