package com.FMDAP.pulsepoint.data.model

data class Host(
    val hostname: String = "",
    val ip: String = "",
    val uptime: Double = 0.0,
    val lastSeen: Long = 0,
    val ping: Double? = null,
    val cpu: Double = 0.0,
    val memory: Double = 0.0,
    val disk: Double? = null,
    val friendlyName: String? = null,
    val sortOrder: Int = 0,
    val rdpUrl: String? = null,
    val tags: String? = null
) {
    val isOnline: Boolean get() = System.currentTimeMillis() / 1000 - lastSeen < 120
    val displayName: String get() = friendlyName?.takeIf { it.isNotBlank() } ?: hostname

    fun uptimeFormatted(): String {
        val s = uptime.toLong()
        val d = s / 86400; val h = (s % 86400) / 3600; val m = (s % 3600) / 60
        return when {
            d > 0 -> "${d}d ${h}h"
            h > 0 -> "${h}h ${m}m"
            else  -> "${m}m"
        }
    }

    fun tagList(): List<String> = tags
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()
}
