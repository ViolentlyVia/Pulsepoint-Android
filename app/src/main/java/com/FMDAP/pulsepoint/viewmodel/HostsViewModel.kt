package com.FMDAP.pulsepoint.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FMDAP.pulsepoint.PulsePointApp
import com.FMDAP.pulsepoint.data.model.Host
import com.FMDAP.pulsepoint.data.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HostsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as PulsePointApp).repository
    private val _state = MutableStateFlow(UiState<List<Host>>())
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repo.getHosts()
            _state.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun moveUp(hostname: String) {
        viewModelScope.launch { repo.moveHostUp(hostname); refresh() }
    }

    fun moveDown(hostname: String) {
        viewModelScope.launch { repo.moveHostDown(hostname); refresh() }
    }
}
