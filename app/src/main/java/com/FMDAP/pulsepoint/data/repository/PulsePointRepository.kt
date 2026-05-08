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

    // Unraid
    suspend fun getUnraid() = requireConfig().mapCatching { it.api.getUnraid() }
    suspend fun refreshUnraid() = requireConfig().mapCatching { it.api.refreshUnraid() }
    suspend fun startContainer(id: String) = requireConfig().mapCatching { it.api.startContainer(id) }
    suspend fun stopContainer(id: String) = requireConfig().mapCatching { it.api.stopContainer(id) }
    suspend fun restartContainer(id: String) = requireConfig().mapCatching { it.api.restartContainer(id) }
    suspend fun startVm(name: String) = requireConfig().mapCatching { it.api.startVm(name) }
    suspend fun stopVm(name: String) = requireConfig().mapCatching { it.api.stopVm(name) }
    suspend fun restartVm(name: String) = requireConfig().mapCatching { it.api.restartVm(name) }

    // iDRAC
    suspend fun getIdrac() = requireConfig().mapCatching { it.api.getIdrac() }
    suspend fun refreshIdrac() = requireConfig().mapCatching { it.api.refreshIdrac() }

    // Omada
    suspend fun getOmada() = requireConfig().mapCatching { it.api.getOmada() }
    suspend fun refreshOmada() = requireConfig().mapCatching { it.api.refreshOmada() }
    suspend fun getOmadaSite(siteId: String) = requireConfig().mapCatching { it.api.getOmadaSite(siteId) }
    suspend fun setPreferredSite(siteId: String) = requireConfig().mapCatching { it.api.setPreferredSite(siteId) }

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

    // Integration settings
    suspend fun getIntegrations() = requireConfig().mapCatching { it.api.getIntegrations() }
    suspend fun updateUnraid(host: String, apiKey: String, apiKeyId: String, bearerToken: String) =
        requireConfig().mapCatching { it.api.updateUnraid(UpdateUnraidRequest(host, apiKey, apiKeyId, bearerToken)) }
    suspend fun updateIdrac(host: String, username: String, password: String) =
        requireConfig().mapCatching { it.api.updateIdrac(UpdateIdracRequest(host, username, password)) }
    suspend fun getOmadaSettings() = requireConfig().mapCatching { it.api.getOmadaSettings() }
    suspend fun updateOmada(baseUrl: String, omadacId: String, clientId: String, clientSecret: String, preferSiteId: String) =
        requireConfig().mapCatching { it.api.updateOmada(UpdateOmadaRequest(baseUrl, omadacId, clientId, clientSecret, preferSiteId)) }
    suspend fun getGrowSettings() = requireConfig().mapCatching { it.api.getGrowSettings() }
    suspend fun updateGrow(url: String, rtspUrl: String, hlsUrl: String) =
        requireConfig().mapCatching { it.api.updateGrow(UpdateGrowRequest(url, rtspUrl, hlsUrl)) }
    suspend fun getAppearance() = requireConfig().mapCatching { it.api.getAppearance() }
    suspend fun updateAppearance(
        accentColor: String, siteName: String, navHidden: String, cardColumns: String,
        hiddenMetrics: String, refreshInterval: Int, onlineThreshold: Int, hideServicesWidget: Boolean
    ) = requireConfig().mapCatching {
        it.api.updateAppearance(UpdateAppearanceRequest(accentColor, siteName, navHidden, cardColumns, hiddenMetrics, refreshInterval, onlineThreshold, hideServicesWidget))
    }

    // Grow
    suspend fun getGrowStatus() = requireConfig().mapCatching { it.api.getGrowStatus() }
    suspend fun controlGrowPump(action: String) = requireConfig().mapCatching { it.api.controlGrowPump(action) }
    suspend fun setGrow(threshold: Int, pumpDur: Int) = requireConfig().mapCatching { it.api.setGrow(threshold, pumpDur) }
    suspend fun clearGrowHistory() = requireConfig().mapCatching { it.api.clearGrowHistory() }
}
