package com.FMDAP.pulsepoint.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FMDAP.pulsepoint.PulsePointApp
import com.FMDAP.pulsepoint.data.model.IdracSnapshot
import com.FMDAP.pulsepoint.data.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IdracViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as PulsePointApp).repository

    private val _state = MutableStateFlow(UiState<IdracSnapshot>())
    val state = _state.asStateFlow()

    init { load(forceRefresh = false) }

    fun refresh() { viewModelScope.launch { load(forceRefresh = true) } }

    private fun load(forceRefresh: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = if (forceRefresh) repo.refreshIdrac() else repo.getIdrac()
            _state.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }
}
