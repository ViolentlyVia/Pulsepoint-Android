package com.FMDAP.pulsepoint.data.model

data class IdracSnapshot(
    val connected: Boolean = false,
    val error: String? = null,
    val fetchedAt: String = "",
    val system: IdracSystem = IdracSystem(),
    val temperatures: List<ThermalSensor> = emptyList(),
    val fans: List<FanInfo> = emptyList(),
    val powerSupplies: List<PowerSupplyInfo> = emptyList(),
    val drives: List<StorageDrive> = emptyList()
)

data class IdracSystem(
    val manufacturer: String = "",
    val model: String = "",
    val serviceTag: String = "",
    val biosVersion: String = "",
    val powerState: String = "",
    val healthStatus: String = "",
    val processorCount: Int = 0,
    val totalMemoryGiB: Double = 0.0,
    val idracFirmware: String = ""
)

data class ThermalSensor(
    val name: String = "",
    val readingCelsius: Double = 0.0,
    val upperThresholdCritical: Double? = null,
    val status: String = ""
)

data class FanInfo(
    val name: String = "",
    val rpm: Int = 0,
    val status: String = ""
)

data class PowerSupplyInfo(
    val name: String = "",
    val model: String = "",
    val lastOutputWatts: Double? = null,
    val powerCapacityWatts: Double? = null,
    val status: String = ""
)

data class StorageDrive(
    val name: String = "",
    val model: String = "",
    val manufacturer: String = "",
    val serialNumber: String = "",
    val mediaType: String = "",
    val protocol: String = "",
    val capacityBytes: Long = 0,
    val health: String = "",
    val state: String = ""
)
