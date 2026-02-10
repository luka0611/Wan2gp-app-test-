package com.example.wan2gpremote.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Wan2gpApi {
    @POST("jobs")
    suspend fun submitJob(@Body request: SubmitJobRequest): SubmitJobResponse

    @GET("jobs/{jobId}")
    suspend fun getJobStatus(@Path("jobId") jobId: String): JobStatusResponse

    @GET("jobs/{jobId}/assets")
    suspend fun getResultAssets(@Path("jobId") jobId: String): ResultAssetsResponse

    @POST("jobs/{jobId}/cancel")
    suspend fun cancelJob(@Path("jobId") jobId: String)

    @POST("jobs/{jobId}/retry")
    suspend fun retryJob(@Path("jobId") jobId: String): RetryJobResponse
}
