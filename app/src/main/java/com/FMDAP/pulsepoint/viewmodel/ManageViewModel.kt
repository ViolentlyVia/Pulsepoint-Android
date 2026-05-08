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

class ManageViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as PulsePointApp).repository

    var isLoggedIn by mutableStateOf(false)
        private set
    var loginError by mutableStateOf<String?>(null)
        private set
    var isLoggingIn by mutableStateOf(false)
        private set

    private val _servicesState = MutableStateFlow(UiState<List<ServiceEntry>>(isLoading = false))
    private val _assetsState   = MutableStateFlow(UiState<List<Host>>(isLoading = false))
    private val _integrationsState = MutableStateFlow(UiState<IntegrationsResponse>(isLoading = false))
    private val _omadaSettingsState = MutableStateFlow(UiState<OmadaSettings>(isLoading = false))
    private val _growSettingsState = MutableStateFlow(UiState<GrowSettings>(isLoading = false))
    private val _appearanceState = MutableStateFlow(UiState<AppearanceSettings>(isLoading = false))

    val servicesState = _servicesState.asStateFlow()
    val assetsState   = _assetsState.asStateFlow()
    val integrationsState = _integrationsState.asStateFlow()
    val omadaSettingsState = _omadaSettingsState.asStateFlow()
    val growSettingsState = _growSettingsState.asStateFlow()
    val appearanceState = _appearanceState.asStateFlow()

    var integrationSaveError by mutableStateOf<String?>(null)
        private set
    var integrationSaveSuccess by mutableStateOf(false)
        private set
    var appearanceSaveError by mutableStateOf<String?>(null)
        private set
    var appearanceSaveSuccess by mutableStateOf(false)
        private set

    fun login(password: String) {
        viewModelScope.launch {
            isLoggingIn = true
            loginError = null
            val result = repo.login(password)
            isLoggingIn = false
            result.fold(
                onSuccess = { isLoggedIn = true },
                onFailure = { loginError = it.message ?: "Login failed" }
            )
        }
    }

    fun logout() {
        repo.logout()
        isLoggedIn = false
    }

    fun loadServices() {
        viewModelScope.launch {
            _servicesState.value = UiState(isLoading = true)
            val result = repo.getManageServices()
            _servicesState.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun addService(name: String, address: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.addService(name, address)
            loadServices()
            onDone()
        }
    }

    fun deleteService(id: Int) {
        viewModelScope.launch { repo.deleteService(id); loadServices() }
    }

    fun loadAssets() {
        viewModelScope.launch {
            _assetsState.value = UiState(isLoading = true)
            val result = repo.getManageAssets()
            _assetsState.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun renameAsset(hostname: String, name: String) {
        viewModelScope.launch { repo.renameAsset(hostname, name); loadAssets() }
    }

    fun loadIntegrations() {
        viewModelScope.launch {
            _integrationsState.value = UiState(isLoading = true)
            val result = repo.getIntegrations()
            _integrationsState.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
        viewModelScope.launch {
            _omadaSettingsState.value = UiState(isLoading = true)
            val result = repo.getOmadaSettings()
            _omadaSettingsState.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
        viewModelScope.launch {
            _growSettingsState.value = UiState(isLoading = true)
            val result = repo.getGrowSettings()
            _growSettingsState.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun saveUnraid(host: String, apiKey: String, apiKeyId: String, bearerToken: String) {
        viewModelScope.launch {
            integrationSaveError = null
            integrationSaveSuccess = false
            repo.updateUnraid(host, apiKey, apiKeyId, bearerToken).fold(
                onSuccess = { integrationSaveSuccess = true },
                onFailure = { integrationSaveError = it.message }
            )
        }
    }

    fun saveIdrac(host: String, username: String, password: String) {
        viewModelScope.launch {
            integrationSaveError = null
            integrationSaveSuccess = false
            repo.updateIdrac(host, username, password).fold(
                onSuccess = { integrationSaveSuccess = true },
                onFailure = { integrationSaveError = it.message }
            )
        }
    }

    fun saveOmada(baseUrl: String, omadacId: String, clientId: String, clientSecret: String, preferSiteId: String) {
        viewModelScope.launch {
            integrationSaveError = null
            integrationSaveSuccess = false
            repo.updateOmada(baseUrl, omadacId, clientId, clientSecret, preferSiteId).fold(
                onSuccess = { integrationSaveSuccess = true },
                onFailure = { integrationSaveError = it.message }
            )
        }
    }

    fun saveGrow(url: String, rtspUrl: String, hlsUrl: String) {
        viewModelScope.launch {
            integrationSaveError = null
            integrationSaveSuccess = false
            repo.updateGrow(url, rtspUrl, hlsUrl).fold(
                onSuccess = { integrationSaveSuccess = true },
                onFailure = { integrationSaveError = it.message }
            )
        }
    }

    fun loadAppearance() {
        viewModelScope.launch {
            _appearanceState.value = UiState(isLoading = true)
            val result = repo.getAppearance()
            _appearanceState.value = result.fold(
                onSuccess = { UiState(data = it, isLoading = false) },
                onFailure = { UiState(error = it.message, isLoading = false) }
            )
        }
    }

    fun saveAppearance(
        accentColor: String, siteName: String, navHidden: String, cardColumns: String,
        hiddenMetrics: String, refreshInterval: Int, onlineThreshold: Int, hideServicesWidget: Boolean
    ) {
        viewModelScope.launch {
            appearanceSaveError = null
            appearanceSaveSuccess = false
            repo.updateAppearance(accentColor, siteName, navHidden, cardColumns, hiddenMetrics, refreshInterval, onlineThreshold, hideServicesWidget).fold(
                onSuccess = { appearanceSaveSuccess = true },
                onFailure = { appearanceSaveError = it.message }
            )
        }
    }

    fun clearIntegrationStatus() {
        integrationSaveError = null
        integrationSaveSuccess = false
    }

    fun clearAppearanceStatus() {
        appearanceSaveError = null
        appearanceSaveSuccess = false
    }
}
