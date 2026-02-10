package com.example.wan2gpremote.network

data class SubmitJobRequest(
    val model: String,
    val parameters: Map<String, Any>
)

data class SubmitJobResponse(
    val jobId: String,
    val status: String? = null,
)

data class JobStatusResponse(
    val jobId: String,
    val status: String,
    val progress: Float? = null,
    val error: String? = null,
)

data class JobAsset(
    val id: String,
    val url: String,
    val mimeType: String? = null,
    val fileName: String? = null,
)

data class ResultAssetsResponse(
    val jobId: String,
    val assets: List<JobAsset>
)

data class RetryJobResponse(
    val jobId: String,
)
