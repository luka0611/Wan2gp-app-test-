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
    val fps: Int = 24,
    val steps: Int = 28,
    val cfgScale: Float = 3.5f,
    val sampler: String = "euler",
    val scheduler: String = "karras",
    val denoiseStrength: Float = 1.0f,
    val prompt: String = "",
    val seed: String = "",
    val randomizeSeed: Boolean = true,
    val negativePrompt: String = "",
    val tiling: Boolean = false,
    val upscale: Boolean = false,
    val upscaleFactor: Float = 1.0f,
)

data class FluxKlein9bOptions(
    val width: Int = 1024,
    val height: Int = 1024,
    val steps: Int = 24,
    val numImages: Int = 1,
    val guidance: Float = 3.5f,
    val sampler: String = "euler",
    val scheduler: String = "karras",
    val strength: Float = 1.0f,
    val denoise: Float = 1.0f,
    val prompt: String = "",
    val negativePrompt: String = "",
    val seed: String = "",
    val randomizeSeed: Boolean = true,
    val safetyChecker: Boolean = true,
    val tiling: Boolean = false,
    val upscale: Boolean = false,
    val upscaleFactor: Float = 1.0f,
)

fun Ltx2Options.validated(): Ltx2Options = copy(
    width = width.coerceIn(256, 2048),
    height = height.coerceIn(256, 2048),
    frames = frames.coerceIn(8, 240),
    fps = fps.coerceIn(1, 60),
    steps = steps.coerceIn(1, 80),
    cfgScale = cfgScale.coerceIn(1f, 20f),
    denoiseStrength = denoiseStrength.coerceIn(0f, 1f),
    seed = seed.filter { it.isDigit() || it == '-' },
    upscaleFactor = upscaleFactor.coerceIn(1f, 4f),
)

fun FluxKlein9bOptions.validated(): FluxKlein9bOptions = copy(
    width = width.coerceIn(256, 2048),
    height = height.coerceIn(256, 2048),
    steps = steps.coerceIn(1, 80),
    numImages = numImages.coerceIn(1, 8),
    guidance = guidance.coerceIn(0f, 20f),
    strength = strength.coerceIn(0f, 1f),
    denoise = denoise.coerceIn(0f, 1f),
    seed = seed.filter { it.isDigit() || it == '-' },
    upscaleFactor = upscaleFactor.coerceIn(1f, 4f),
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
