package com.example.wan2gpremote.data.repository

import com.example.wan2gpremote.data.storage.GalleryStorage
import com.example.wan2gpremote.domain.GenerationPayload
import com.example.wan2gpremote.network.JobStatusResponse
import com.example.wan2gpremote.network.NetworkClientFactory
import com.example.wan2gpremote.network.SubmitJobRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class GenerationRepository(
    private val galleryStorage: GalleryStorage
) {

    private val downloadClient = OkHttpClient()

    suspend fun submitJob(serverIp: String, payload: GenerationPayload): String {
        return runNetwork { 
            val api = NetworkClientFactory.createApi(serverIp)
            api.submitJob(SubmitJobRequest(payload.model, payload.parameters)).jobId
        }
    }

    suspend fun pollUntilTerminal(serverIp: String, jobId: String): JobStatusResponse {
        return runNetwork {
            val api = NetworkClientFactory.createApi(serverIp)
            while (true) {
                val status = api.getJobStatus(jobId)
                when (status.status.lowercase()) {
                    "completed", "failed", "cancelled" -> return@runNetwork status
                    else -> delay(2_000)
                }
            }
        }
    }

    suspend fun cancelJob(serverIp: String, jobId: String) {
        runNetwork {
            val api = NetworkClientFactory.createApi(serverIp)
            api.cancelJob(jobId)
        }
    }

    suspend fun retryJob(serverIp: String, jobId: String): String {
        return runNetwork {
            val api = NetworkClientFactory.createApi(serverIp)
            api.retryJob(jobId).jobId
        }
    }

    suspend fun fetchAndPersistAssets(serverIp: String, jobId: String): List<String> {
        return runNetwork {
            val api = NetworkClientFactory.createApi(serverIp)
            val assets = api.getResultAssets(jobId).assets
            assets.mapIndexed { index, asset ->
                val fileName = asset.fileName ?: "wan2gp_${jobId}_$index"
                val mimeType = asset.mimeType ?: "application/octet-stream"
                val bytes = downloadBytes(resolveAssetUrl(serverIp, asset.url))
                galleryStorage.saveAsset(bytes, fileName, mimeType).toString()
            }
        }
    }

    private fun resolveAssetUrl(serverIp: String, rawUrl: String): String {
        return if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            rawUrl
        } else {
            "${NetworkClientFactory.normalizeBaseUrl(serverIp).removeSuffix("/")}/${rawUrl.removePrefix("/")}"
        }
    }

    private suspend fun downloadBytes(url: String): ByteArray = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).get().build()
        downloadClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Asset download failed: HTTP ${response.code}")
            response.body?.bytes() ?: throw IOException("Asset response body is empty")
        }
    }

    private suspend fun <T> runNetwork(block: suspend () -> T): T {
        return try {
            block()
        } catch (e: IllegalArgumentException) {
            throw GenerationNetworkException.InvalidIp(e.message ?: "Invalid LAN IP")
        } catch (e: SocketTimeoutException) {
            throw GenerationNetworkException.Timeout
        } catch (e: HttpException) {
            throw GenerationNetworkException.Http(e.code(), e.message())
        } catch (e: IOException) {
            throw GenerationNetworkException.UnreachableHost(e.message ?: "Unable to reach host")
        } catch (e: Exception) {
            throw GenerationNetworkException.MalformedResponse(e.message ?: "Malformed server response")
        }
    }
}

sealed class GenerationNetworkException(message: String) : Exception(message) {
    class InvalidIp(message: String) : GenerationNetworkException(message)
    data object Timeout : GenerationNetworkException("Request timed out")
    class UnreachableHost(message: String) : GenerationNetworkException(message)
    class Http(val code: Int, override val message: String) : GenerationNetworkException("HTTP $code: $message")
    class MalformedResponse(message: String) : GenerationNetworkException(message)
}
