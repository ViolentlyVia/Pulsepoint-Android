package com.FMDAP.pulsepoint.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FMDAP.pulsepoint.PulsePointApp
import com.FMDAP.pulsepoint.data.model.UiState
import com.FMDAP.pulsepoint.data.model.VersionInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo  = (app as PulsePointApp).repository
    private val prefs = (app as PulsePointApp).prefs

    var serverUrl      by mutableStateOf("")
    var apiKey         by mutableStateOf("")
    var managePassword by mutableStateOf("")
    var saveSuccess    by mutableStateOf(false)
    var versionState   by mutableStateOf(UiState<VersionInfo>(isLoading = false))

    init {
        viewModelScope.launch {
            serverUrl      = prefs.serverUrl.first()
            apiKey         = prefs.apiKey.first()
            managePassword = prefs.managePassword.first()
        }
    }

    fun save() {
        viewModelScope.launch {
            prefs.saveServerUrl(serverUrl.trim())
            prefs.saveApiKey(apiKey.trim())
            prefs.saveManagePassword(managePassword)
            saveSuccess = true
        }
    }

    fun fetchVersion() {
        viewModelScope.launch {
            versionState = UiState(isLoading = true)
            val result = repo.getVersion()
            versionState = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }
}
