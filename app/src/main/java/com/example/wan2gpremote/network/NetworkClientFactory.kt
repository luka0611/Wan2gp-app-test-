package com.example.wan2gpremote.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClientFactory {
    fun createApi(baseUrl: String): Wan2gpApi {
        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp)
            .build()
            .create(Wan2gpApi::class.java)
    }

    fun normalizeBaseUrl(serverIp: String): String {
        val trimmed = serverIp.trim().removePrefix("http://").removePrefix("https://")
        require(trimmed.matches(Regex("^[a-zA-Z0-9.-]+(:\\d{1,5})?$"))) {
            "Invalid LAN IP/host format. Expected e.g. 192.168.1.25:7860"
        }

        val port = trimmed.substringAfter(':', "")
        if (port.isNotEmpty()) {
            val value = port.toIntOrNull()
            require(value != null && value in 1..65535) { "Invalid port in LAN IP" }
        }

        return "http://$trimmed/"
    }
}
