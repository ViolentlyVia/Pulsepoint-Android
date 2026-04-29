package com.FMDAP.pulsepoint.data.model

data class ServiceStatus(
    val name: String = "",
    val url: String = "",
    val online: Boolean = false,
    val statusCode: Int? = null,
    val responseMs: Double? = null,
    val offlineSince: Long? = null,
    val error: String? = null
)
