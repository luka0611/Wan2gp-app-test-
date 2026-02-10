package com.example.wan2gpremote.gallery

data class GenerationRecord(
    val id: String,
    val model: String,
    val mode: String,
    val prompt: String,
    val outputType: OutputType,
    val localPathOrUri: String,
    val createdAt: Long,
    val serverJobId: String,
)

enum class OutputType {
    IMAGE,
    VIDEO
}
