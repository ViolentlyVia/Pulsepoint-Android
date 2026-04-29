package com.FMDAP.pulsepoint.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FMDAP.pulsepoint.PulsePointApp
import com.FMDAP.pulsepoint.data.model.UnraidSnapshot
import com.FMDAP.pulsepoint.data.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UnraidViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as PulsePointApp).repository

    private val _state = MutableStateFlow(UiState<UnraidSnapshot>())
    val state = _state.asStateFlow()

    init { load(forceRefresh = false) }

    fun refresh() { viewModelScope.launch { load(forceRefresh = true) } }

    private fun load(forceRefresh: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = if (forceRefresh) repo.refreshUnraid() else repo.getUnraid()
            _state.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun startContainer(id: String) {
        viewModelScope.launch {
            repo.startContainer(id)
            load(forceRefresh = true)
        }
    }

    fun stopContainer(id: String) {
        viewModelScope.launch {
            repo.stopContainer(id)
            load(forceRefresh = true)
        }
    }

    fun restartContainer(id: String) {
        viewModelScope.launch {
            repo.restartContainer(id)
            load(forceRefresh = true)
        }
    }

    fun startVm(name: String) {
        viewModelScope.launch {
            repo.startVm(name)
            load(forceRefresh = true)
        }
    }

    fun stopVm(name: String) {
        viewModelScope.launch {
            repo.stopVm(name)
            load(forceRefresh = true)
        }
    }

    fun restartVm(name: String) {
        viewModelScope.launch {
            repo.restartVm(name)
            load(forceRefresh = true)
        }
    }
}
