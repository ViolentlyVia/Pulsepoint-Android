package com.FMDAP.pulsepoint.data.model

data class Summary(
    val hosts: HostsSummary = HostsSummary(),
    val services: ServicesSummary = ServicesSummary(),
    val generatedAt: Long = 0
)

data class HostsSummary(
    val total: Int = 0,
    val online: Int = 0,
    val offline: Int = 0,
    val list: List<Host> = emptyList()
)

data class ServicesSummary(
    val total: Int = 0,
    val online: Int = 0,
    val offline: Int = 0,
    val list: List<ServiceStatus> = emptyList()
)
