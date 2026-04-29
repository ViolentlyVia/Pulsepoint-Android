package com.FMDAP.pulsepoint.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FMDAP.pulsepoint.PulsePointApp
import com.FMDAP.pulsepoint.data.model.ServiceStatus
import com.FMDAP.pulsepoint.data.model.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServicesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as PulsePointApp).repository
    private val _state = MutableStateFlow(UiState<List<ServiceStatus>>())
    val state = _state.asStateFlow()

    init { startAutoRefresh() }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                load(forceRefresh = false)
                delay(30_000)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { load(forceRefresh = true) }
    }

    private suspend fun load(forceRefresh: Boolean) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        val result = if (forceRefresh) repo.refreshServices() else repo.getServices()
        _state.value = result.fold(
            onSuccess = { UiState(data = it, isLoading = false) },
            onFailure = { UiState(error = it.message, isLoading = false) }
        )
    }
}
