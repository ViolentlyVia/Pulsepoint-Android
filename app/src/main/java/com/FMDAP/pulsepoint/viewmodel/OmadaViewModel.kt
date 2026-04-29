package com.FMDAP.pulsepoint.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FMDAP.pulsepoint.PulsePointApp
import com.FMDAP.pulsepoint.data.model.OmadaSnapshot
import com.FMDAP.pulsepoint.data.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OmadaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as PulsePointApp).repository

    private val _state = MutableStateFlow(UiState<OmadaSnapshot>())
    val state = _state.asStateFlow()

    init { load(forceRefresh = false) }

    fun refresh() { viewModelScope.launch { load(forceRefresh = true) } }

    private fun load(forceRefresh: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = if (forceRefresh) repo.refreshOmada() else repo.getOmada()
            _state.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun selectSite(siteId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = repo.getOmadaSite(siteId)
            _state.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun setPreferredSite(siteId: String) {
        viewModelScope.launch { repo.setPreferredSite(siteId) }
    }
}
