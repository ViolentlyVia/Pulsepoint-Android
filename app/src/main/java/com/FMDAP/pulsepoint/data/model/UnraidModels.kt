package com.FMDAP.pulsepoint.data.model

data class UnraidSnapshot(
    val connected: Boolean = false,
    val error: String? = null,
    val fetchedAt: String = "",
    val array: UnraidArray = UnraidArray(),
    val disks: List<DiskInfo> = emptyList(),
    val parities: List<DiskInfo> = emptyList(),
    val containers: List<DockerContainer> = emptyList(),
    val vms: List<VmDomain> = emptyList(),
    val shares: List<ShareInfo> = emptyList()
)

data class UnraidArray(
    val state: String = "",
    val usedBytes: Long = 0,
    val freeBytes: Long = 0,
    val totalBytes: Long = 0
)

data class DiskInfo(
    val name: String = "",
    val device: String = "",
    val status: String = "",
    val temp: Double = 0.0,
    val size: Long = 0,
    val type: String = ""
)

data class DockerContainer(
    val id: String = "",
    val names: String = "",
    val image: String = "",
    val state: String = "",
    val status: String = "",
    val running: Boolean = false
)

data class VmDomain(
    val name: String = "",
    val state: String = "",
    val running: Boolean = false
)

data class ShareInfo(
    val name: String = "",
    val freeKb: Long = 0,
    val sizeKb: Long = 0,
    val usedKb: Long = 0
)
