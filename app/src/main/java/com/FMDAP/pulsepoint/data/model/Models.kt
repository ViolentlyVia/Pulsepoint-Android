package com.FMDAP.pulsepoint.data.model

import com.google.gson.annotations.SerializedName

data class PingResult(
    val ip: String = "",
    @SerializedName("ping_ms") val pingMs: Double? = null,
    val online: Boolean = false
)

data class VersionInfo(
    val version: String = "",
    val dotnet: String = "",
    val pid: Int = 0,
    @SerializedName("uptime_s") val uptimeS: Long = 0
)

data class OkResponse(val ok: Boolean = false)

data class AddServiceRequest(val name: String, val address: String)

data class AssetUpdateRequest(
    val friendlyName: String?,
    val ip: String?,
    val rdpUrl: String?,
    val tags: String?
)

data class RenameRequest(val name: String)

data class UiState<T>(
    val data: T? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
