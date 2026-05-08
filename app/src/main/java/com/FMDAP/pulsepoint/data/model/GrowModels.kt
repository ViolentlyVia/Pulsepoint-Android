package com.FMDAP.pulsepoint.data.model

import com.google.gson.annotations.SerializedName

data class GrowStatusResponse(
    val configured: Boolean = false,
    val connected: Boolean = false,
    val error: String? = null,
    val data: GrowData? = null
)

data class GrowData(
    val moisture: Int = 0,
    val temperature: Float = 0f,
    val humidity: Int = 0,
    @SerializedName("pump_on") val pumpOn: Boolean = false,
    val threshold: Int = 40,
    @SerializedName("pump_duration_s") val pumpDurationS: Int = 5,
    @SerializedName("hist_moisture_12h") val histMoisture12h: List<Float> = emptyList(),
    @SerializedName("hist_moisture_1d")  val histMoisture1d: List<Float>  = emptyList(),
    @SerializedName("hist_moisture_1w")  val histMoisture1w: List<Float>  = emptyList(),
    @SerializedName("hist_temp_12h")     val histTemp12h: List<Float>     = emptyList(),
    @SerializedName("hist_temp_1d")      val histTemp1d: List<Float>      = emptyList(),
    @SerializedName("hist_temp_1w")      val histTemp1w: List<Float>      = emptyList(),
    @SerializedName("hist_hum_12h")      val histHum12h: List<Float>      = emptyList(),
    @SerializedName("hist_hum_1d")       val histHum1d: List<Float>       = emptyList(),
    @SerializedName("hist_hum_1w")       val histHum1w: List<Float>       = emptyList()
)

data class GrowSettings(
    val url: String = "",
    val rtspUrl: String = "",
    val hlsUrl: String = "",
    val configured: Boolean = false
)

data class UpdateGrowRequest(
    val url: String,
    val rtspUrl: String,
    val hlsUrl: String
)

data class AppearanceSettings(
    val accentColor: String = "#7c3aed",
    val siteName: String = "PulsePoint",
    val navHidden: String = "",
    val cardColumns: String = "auto",
    val hiddenMetrics: String = "",
    val refreshInterval: Int = 15,
    val onlineThreshold: Int = 120,
    val hideServicesWidget: Boolean = false
)

data class UpdateAppearanceRequest(
    val accentColor: String,
    val siteName: String,
    val navHidden: String,
    val cardColumns: String,
    val hiddenMetrics: String,
    val refreshInterval: Int,
    val onlineThreshold: Int,
    val hideServicesWidget: Boolean
)
