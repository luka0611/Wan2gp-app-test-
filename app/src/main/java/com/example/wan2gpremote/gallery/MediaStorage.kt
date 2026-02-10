package com.example.wan2gpremote.gallery

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class MediaStorage(private val context: Context) {
    suspend fun maybeDownloadToAppStorage(source: String, outputType: OutputType): String {
        if (source.isBlank()) return ""
        if (!source.startsWith("http://") && !source.startsWith("https://")) {
            return source
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                val extension = if (outputType == OutputType.VIDEO) "mp4" else "jpg"
                val galleryDir = File(context.filesDir, "gallery").apply { mkdirs() }
                val destination = File(galleryDir, "gen_${System.currentTimeMillis()}.$extension")
                URL(source).openStream().use { input ->
                    destination.outputStream().use { output -> input.copyTo(output) }
                }
                Uri.fromFile(destination).toString()
            }.getOrDefault(source)
        }
    }
}
