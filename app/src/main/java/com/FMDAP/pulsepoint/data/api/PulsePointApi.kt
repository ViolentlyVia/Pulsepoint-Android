package com.FMDAP.pulsepoint.data.api

import com.FMDAP.pulsepoint.data.model.*
import retrofit2.http.*

interface PulsePointApi {

    // --- Dashboard routes (require X-Api-Key) ---

    @GET("api/hosts")
    suspend fun getHosts(): List<Host>

    @GET("api/hosts/{hostname}")
    suspend fun getHost(@Path("hostname") hostname: String): Host

    @GET("api/hosts/{hostname}/ping")
    suspend fun pingHost(@Path("hostname") hostname: String): PingResult

    @PUT("api/assets/{hostname}")
    suspend fun updateAsset(
        @Path("hostname") hostname: String,
        @Body body: AssetUpdateRequest
    ): OkResponse

    @DELETE("api/assets/{hostname}")
    suspend fun deleteAsset(@Path("hostname") hostname: String): OkResponse

    @POST("api/assets/{hostname}/move-up")
    suspend fun moveHostUp(@Path("hostname") hostname: String): OkResponse

    @POST("api/assets/{hostname}/move-down")
    suspend fun moveHostDown(@Path("hostname") hostname: String): OkResponse

    @GET("api/services")
    suspend fun getServices(): List<ServiceStatus>

    @GET("api/services/refresh")
    suspend fun refreshServices(): List<ServiceStatus>

    @GET("api/summary")
    suspend fun getSummary(): Summary

    @GET("api/version")
    suspend fun getVersion(): VersionInfo

    // --- Integration routes (require X-Api-Key) ---

    @GET("api/unraid")
    suspend fun getUnraid(): UnraidSnapshot

    @GET("api/unraid/refresh")
    suspend fun refreshUnraid(): UnraidSnapshot

    @POST("api/unraid/docker/{id}/start")
    suspend fun startContainer(@Path("id") id: String): OkResponse

    @POST("api/unraid/docker/{id}/stop")
    suspend fun stopContainer(@Path("id") id: String): OkResponse

    @POST("api/unraid/docker/{id}/restart")
    suspend fun restartContainer(@Path("id") id: String): OkResponse

    @POST("api/unraid/vm/{name}/start")
    suspend fun startVm(@Path("name") name: String): OkResponse

    @POST("api/unraid/vm/{name}/stop")
    suspend fun stopVm(@Path("name") name: String): OkResponse

    @POST("api/unraid/vm/{name}/restart")
    suspend fun restartVm(@Path("name") name: String): OkResponse

    @GET("api/idrac")
    suspend fun getIdrac(): IdracSnapshot

    @GET("api/idrac/refresh")
    suspend fun refreshIdrac(): IdracSnapshot

    @GET("api/omada")
    suspend fun getOmada(): OmadaSnapshot

    @GET("api/omada/refresh")
    suspend fun refreshOmada(): OmadaSnapshot

    @GET("api/omada/site/{siteId}")
    suspend fun getOmadaSite(@Path("siteId") siteId: String): OmadaSnapshot

    @PUT("api/omada/preferred-site/{siteId}")
    suspend fun setPreferredSite(@Path("siteId") siteId: String): OkResponse

    // --- Management routes (require pp_session cookie) ---

    @GET("api/manage/services")
    suspend fun getManageServices(): List<ServiceEntry>

    @POST("api/manage/services")
    suspend fun addService(@Body body: AddServiceRequest): OkResponse

    @DELETE("api/manage/services/{id}")
    suspend fun deleteService(@Path("id") id: Int): OkResponse

    @PUT("api/manage/assets/{hostname}/name")
    suspend fun renameAsset(
        @Path("hostname") hostname: String,
        @Body body: RenameRequest
    ): OkResponse

    @GET("api/manage/assets")
    suspend fun getManageAssets(): List<Host>

    @GET("api/manage/integrations")
    suspend fun getIntegrations(): IntegrationsResponse

    @PUT("api/manage/integrations/unraid")
    suspend fun updateUnraid(@Body body: UpdateUnraidRequest): OkResponse

    @PUT("api/manage/integrations/idrac")
    suspend fun updateIdrac(@Body body: UpdateIdracRequest): OkResponse

    @GET("api/manage/integrations/omada")
    suspend fun getOmadaSettings(): OmadaSettings

    @PUT("api/manage/integrations/omada")
    suspend fun updateOmada(@Body body: UpdateOmadaRequest): OkResponse

    @GET("api/manage/integrations/grow")
    suspend fun getGrowSettings(): GrowSettings

    @PUT("api/manage/integrations/grow")
    suspend fun updateGrow(@Body body: UpdateGrowRequest): OkResponse

    @GET("api/manage/appearance")
    suspend fun getAppearance(): AppearanceSettings

    @PUT("api/manage/appearance")
    suspend fun updateAppearance(@Body body: UpdateAppearanceRequest): OkResponse

    // --- Grow integration routes (require X-Api-Key) ---

    @GET("api/grow/status")
    suspend fun getGrowStatus(): GrowStatusResponse

    @FormUrlEncoded
    @POST("api/grow/pump")
    suspend fun controlGrowPump(@Field("action") action: String): OkResponse

    @FormUrlEncoded
    @POST("api/grow/set")
    suspend fun setGrow(
        @Field("threshold") threshold: Int,
        @Field("pump_dur") pumpDur: Int
    ): OkResponse

    @HTTP(method = "POST", path = "api/grow/history/clear", hasBody = false)
    suspend fun clearGrowHistory(): OkResponse
}
