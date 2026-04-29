package com.FMDAP.pulsepoint.data.model

data class IntegrationsResponse(
    val unraid: UnraidSettings = UnraidSettings(),
    val idrac: IdracSettings = IdracSettings()
)

data class UnraidSettings(
    val host: String = "",
    val apiKey: String = "",
    val apiKeyId: String = "",
    val bearerToken: String = ""
)

data class IdracSettings(
    val host: String = "",
    val username: String = "",
    val hasPassword: Boolean = false
)

data class OmadaSettings(
    val baseUrl: String = "",
    val omadacId: String = "",
    val clientId: String = "",
    val hasSecret: Boolean = false,
    val preferSiteId: String = ""
)

data class UpdateUnraidRequest(
    val host: String,
    val apiKey: String,
    val apiKeyId: String,
    val bearerToken: String
)

data class UpdateIdracRequest(
    val host: String,
    val username: String,
    val password: String
)

data class UpdateOmadaRequest(
    val baseUrl: String,
    val omadacId: String,
    val clientId: String,
    val clientSecret: String,
    val preferSiteId: String
)
