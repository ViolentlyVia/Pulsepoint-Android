package com.FMDAP.pulsepoint.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FMDAP.pulsepoint.PulsePointApp
import com.FMDAP.pulsepoint.data.model.Host
import com.FMDAP.pulsepoint.data.model.ServiceEntry
import com.FMDAP.pulsepoint.data.model.UiState
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
    val servicesState = _servicesState.asStateFlow()
    val assetsState   = _assetsState.asStateFlow()

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
}
