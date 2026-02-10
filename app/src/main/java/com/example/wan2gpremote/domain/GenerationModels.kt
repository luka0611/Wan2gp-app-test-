package com.example.wan2gpremote.domain

enum class ModelType(val id: String, val label: String) {
    LTX2("ltx2", "LTX 2"),
    FLUX_KLEIN_9B("flux_klein_9b", "Flux Klein 9b"),
    ACE_STEP_15("ace_step_15", "Ace Step 1.5")
}

data class Ltx2Options(
    val width: Int = 1024,
    val height: Int = 576,
    val frames: Int = 73,
    val steps: Int = 28,
    val cfgScale: Float = 3.5f,
    val seed: String = "",
    val negativePrompt: String = "",
)

data class FluxKlein9bOptions(
    val width: Int = 1024,
    val height: Int = 1024,
    val steps: Int = 24,
    val guidance: Float = 3.5f,
    val sampler: String = "euler",
    val seed: String = "",
    val safetyChecker: Boolean = true,
)

data class AceStep15Options(
    val width: Int = 1280,
    val height: Int = 720,
    val durationSeconds: Int = 8,
    val fps: Int = 24,
    val steps: Int = 30,
    val guidance: Float = 4.0f,
    val audioReactive: Boolean = false,
    val seed: String = "",
)

data class GenerationSettings(
    val serverIp: String = "",
    val selectedModel: ModelType = ModelType.LTX2,
    val ltx2: Ltx2Options = Ltx2Options(),
    val flux: FluxKlein9bOptions = FluxKlein9bOptions(),
    val ace: AceStep15Options = AceStep15Options(),
)
