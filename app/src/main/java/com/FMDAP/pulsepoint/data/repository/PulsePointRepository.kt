package com.FMDAP.pulsepoint.data.repository

import com.FMDAP.pulsepoint.data.api.ApiClient
import com.FMDAP.pulsepoint.data.model.*
import com.FMDAP.pulsepoint.data.prefs.AppPreferences
import kotlinx.coroutines.flow.first

class PulsePointRepository(private val prefs: AppPreferences) {

    private var client: ApiClient? = null
    private var cachedUrl = ""
    private var cachedKey = ""

    private suspend fun client(): ApiClient {
        val url = prefs.serverUrl.first().trimEnd('/') + "/"
        val key = prefs.apiKey.first()
        if (client == null || url != cachedUrl || key != cachedKey) {
            client = ApiClient(url, key)
            cachedUrl = url
            cachedKey = key
        }
        return client!!
    }

    private fun configError(): Result<Nothing> =
        Result.failure(Exception("Server URL and API key must be set in Settings"))

    private suspend fun requireConfig(): Result<ApiClient> {
        val url = prefs.serverUrl.first()
        val key = prefs.apiKey.first()
        if (url.isBlank() || key.isBlank()) return configError()
        return Result.success(client())
    }

    suspend fun getSummary()  = requireConfig().mapCatching { it.api.getSummary() }
    suspend fun getHosts()    = requireConfig().mapCatching { it.api.getHosts() }
    suspend fun getHost(h: String) = requireConfig().mapCatching { it.api.getHost(h) }
    suspend fun pingHost(h: String) = requireConfig().mapCatching { it.api.pingHost(h) }
    suspend fun getServices() = requireConfig().mapCatching { it.api.getServices() }
    suspend fun refreshServices() = requireConfig().mapCatching { it.api.refreshServices() }
    suspend fun getVersion()  = requireConfig().mapCatching { it.api.getVersion() }

    suspend fun updateAsset(hostname: String, body: AssetUpdateRequest) =
        requireConfig().mapCatching { it.api.updateAsset(hostname, body) }

    suspend fun deleteAsset(hostname: String) =
        requireConfig().mapCatching { it.api.deleteAsset(hostname) }

    suspend fun moveHostUp(hostname: String) =
        requireConfig().mapCatching { it.api.moveHostUp(hostname) }

    suspend fun moveHostDown(hostname: String) =
        requireConfig().mapCatching { it.api.moveHostDown(hostname) }

    // Management
    suspend fun login(password: String): Result<Unit> {
        val url = prefs.serverUrl.first()
        if (url.isBlank()) return Result.failure(Exception("Server URL is not set"))
        return client().login(password)
    }

    fun logout() { client?.logout() }
    fun isLoggedIn() = client?.cookieJar?.hasSession() ?: false

    suspend fun getManageServices() = requireConfig().mapCatching { it.api.getManageServices() }
    suspend fun addService(name: String, address: String) =
        requireConfig().mapCatching { it.api.addService(AddServiceRequest(name, address)) }
    suspend fun deleteService(id: Int) =
        requireConfig().mapCatching { it.api.deleteService(id) }
    suspend fun getManageAssets() = requireConfig().mapCatching { it.api.getManageAssets() }
    suspend fun renameAsset(hostname: String, name: String) =
        requireConfig().mapCatching { it.api.renameAsset(hostname, RenameRequest(name)) }
}
