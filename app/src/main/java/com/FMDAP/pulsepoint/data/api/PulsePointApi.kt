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
}
