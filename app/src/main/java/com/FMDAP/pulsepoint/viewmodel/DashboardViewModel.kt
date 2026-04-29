package com.FMDAP.pulsepoint.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FMDAP.pulsepoint.PulsePointApp
import com.FMDAP.pulsepoint.data.model.Summary
import com.FMDAP.pulsepoint.data.model.UiState
import com.FMDAP.pulsepoint.data.model.VersionInfo
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as PulsePointApp).repository

    private val _state       = MutableStateFlow(UiState<Summary>())
    private val _versionState = MutableStateFlow(UiState<VersionInfo>(isLoading = false))
    val state        = _state.asStateFlow()
    val versionState = _versionState.asStateFlow()

    init { startAutoRefresh() }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                load()
                delay(30_000)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)

        // Fetch summary and version in parallel
        val summaryDeferred = viewModelScope.async { repo.getSummary() }
        val versionDeferred = viewModelScope.async { repo.getVersion() }

        _state.value = summaryDeferred.await().fold(
            onSuccess = { UiState(data = it, isLoading = false) },
            onFailure = { UiState(error = it.message, isLoading = false) }
        )
        _versionState.value = versionDeferred.await().fold(
            onSuccess = { UiState(data = it, isLoading = false) },
            onFailure = { UiState(error = null, isLoading = false) } // silent fail — version is non-critical
        )
    }
}
