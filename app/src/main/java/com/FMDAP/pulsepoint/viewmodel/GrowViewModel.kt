package com.FMDAP.pulsepoint.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FMDAP.pulsepoint.PulsePointApp
import com.FMDAP.pulsepoint.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GrowViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as PulsePointApp).repository

    private val _statusState = MutableStateFlow(UiState<GrowStatusResponse>(isLoading = false))
    val statusState = _statusState.asStateFlow()

    // Fully-formed HLS proxy URL with API key appended — ready to feed directly to ExoPlayer
    private val _cameraUrl = MutableStateFlow<String?>(null)
    val cameraUrl = _cameraUrl.asStateFlow()

    // Fallback RTSP URL shown when no HLS proxy is available
    private val _rtspUrl = MutableStateFlow<String?>(null)
    val rtspUrl = _rtspUrl.asStateFlow()

    var streamLoadAttempted by mutableStateOf(false)
        private set

    var actionError by mutableStateOf<String?>(null)
        private set
    var actionSuccess by mutableStateOf<String?>(null)
        private set

    init { load() }

    fun load() {
        viewModelScope.launch {
            _statusState.value = UiState(isLoading = true)
            val result = repo.getGrowStatus()
            _statusState.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
        viewModelScope.launch {
            repo.getGrowRelayStream().onSuccess { stream ->
                val key = repo.getApiKey()
                _cameraUrl.value = stream.hlsProxy?.let { proxy ->
                    if (key.isNotBlank()) "$proxy?key=$key" else proxy
                } ?: stream.hls?.let { hls ->
                    if (key.isNotBlank()) "$hls?key=$key" else hls
                }
                _rtspUrl.value = stream.rtsp
            }
            streamLoadAttempted = true
        }
    }

    fun controlPump(start: Boolean) {
        viewModelScope.launch {
            actionError = null
            actionSuccess = null
            repo.controlGrowPump(if (start) "start" else "stop").fold(
                onSuccess = { actionSuccess = if (start) "Pump started" else "Pump stopped"; load() },
                onFailure = { actionError = it.message }
            )
        }
    }

    fun saveSettings(threshold: Int, pumpDur: Int) {
        viewModelScope.launch {
            actionError = null
            actionSuccess = null
            repo.setGrow(threshold, pumpDur).fold(
                onSuccess = { actionSuccess = "Settings saved"; load() },
                onFailure = { actionError = it.message }
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            actionError = null
            actionSuccess = null
            repo.clearGrowHistory().fold(
                onSuccess = { actionSuccess = "History cleared"; load() },
                onFailure = { actionError = it.message }
            )
        }
    }

    fun clearStatus() {
        actionError = null
        actionSuccess = null
    }
}
