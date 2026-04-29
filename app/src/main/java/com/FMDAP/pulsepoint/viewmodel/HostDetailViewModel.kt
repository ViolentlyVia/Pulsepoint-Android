package com.FMDAP.pulsepoint.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FMDAP.pulsepoint.PulsePointApp
import com.FMDAP.pulsepoint.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HostDetailViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as PulsePointApp).repository

    private val _state    = MutableStateFlow(UiState<Host>())
    private val _pingState = MutableStateFlow(UiState<PingResult>(isLoading = false))
    val state     = _state.asStateFlow()
    val pingState = _pingState.asStateFlow()

    fun load(hostname: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repo.getHost(hostname)
            _state.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun ping(hostname: String) {
        viewModelScope.launch {
            _pingState.value = UiState(isLoading = true)
            val result = repo.pingHost(hostname)
            _pingState.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun updateAsset(hostname: String, body: AssetUpdateRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.updateAsset(hostname, body)
            load(hostname)
            onDone()
        }
    }

    fun deleteAsset(hostname: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deleteAsset(hostname)
            onDone()
        }
    }

    fun moveUp(hostname: String) {
        viewModelScope.launch { repo.moveHostUp(hostname); load(hostname) }
    }

    fun moveDown(hostname: String) {
        viewModelScope.launch { repo.moveHostDown(hostname); load(hostname) }
    }
}
