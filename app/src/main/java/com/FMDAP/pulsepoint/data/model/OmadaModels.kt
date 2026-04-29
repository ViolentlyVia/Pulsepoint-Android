package com.FMDAP.pulsepoint.data.model

data class OmadaSnapshot(
    val connected: Boolean = false,
    val error: String? = null,
    val fetchedAt: String = "",
    val sites: List<OmadaSite> = emptyList(),
    val selectedSite: OmadaSite? = null,
    val devices: List<OmadaDevice> = emptyList(),
    val clients: List<OmadaClient> = emptyList()
)

data class OmadaSite(
    val siteId: String = "",
    val name: String = "",
    val scenario: String = ""
)

data class OmadaDevice(
    val mac: String = "",
    val name: String = "",
    val type: String = "",
    val ip: String = "",
    val model: String = "",
    val firmwareVersion: String = "",
    val status: Int = 0,
    val online: Boolean = false,
    val uptime: Long = 0,
    val clientCount: Int = 0,
    val download: Long = 0,
    val upload: Long = 0
)

data class OmadaClient(
    val mac: String = "",
    val name: String = "",
    val ip: String = "",
    val networkName: String = "",
    val ssid: String = "",
    val wireless: Boolean = false,
    val signalLevel: Int = 0,
    val rxRate: Long = 0,
    val txRate: Long = 0,
    val wiredLinkSpeed: Long = 0,
    val uptime: Long = 0,
    val active: Boolean = false,
    val trafficDown: Long = 0,
    val trafficUp: Long = 0,
    val trafficTotal: Long = 0
)
