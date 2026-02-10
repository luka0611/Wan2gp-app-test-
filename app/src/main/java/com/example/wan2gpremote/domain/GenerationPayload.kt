package com.example.wan2gpremote.domain

data class GenerationPayload(
    val model: String,
    val parameters: Map<String, Any>
)

fun GenerationSettings.toPayload(): GenerationPayload {
    val params = when (selectedModel) {
        ModelType.LTX2 -> mapOf(
            "width" to ltx2.width,
            "height" to ltx2.height,
            "frames" to ltx2.frames,
            "fps" to ltx2.fps,
            "steps" to ltx2.steps,
            "cfg_scale" to ltx2.cfgScale,
            "sampler" to ltx2.sampler,
            "scheduler" to ltx2.scheduler,
            "denoise_strength" to ltx2.denoiseStrength,
            "prompt" to ltx2.prompt,
            "seed" to ltx2.seed,
            "randomize_seed" to ltx2.randomizeSeed,
            "negative_prompt" to ltx2.negativePrompt,
            "tiling" to ltx2.tiling,
            "upscale" to ltx2.upscale,
            "upscale_factor" to ltx2.upscaleFactor,
        )

        ModelType.FLUX_KLEIN_9B -> mapOf(
            "width" to flux.width,
            "height" to flux.height,
            "steps" to flux.steps,
            "num_images" to flux.numImages,
            "guidance" to flux.guidance,
            "sampler" to flux.sampler,
            "scheduler" to flux.scheduler,
            "strength" to flux.strength,
            "denoise" to flux.denoise,
            "prompt" to flux.prompt,
            "negative_prompt" to flux.negativePrompt,
            "seed" to flux.seed,
            "randomize_seed" to flux.randomizeSeed,
            "safety_checker" to flux.safetyChecker,
            "tiling" to flux.tiling,
            "upscale" to flux.upscale,
            "upscale_factor" to flux.upscaleFactor,
        )

        ModelType.ACE_STEP_15 -> mapOf(
            "width" to ace.width,
            "height" to ace.height,
            "duration_seconds" to ace.durationSeconds,
            "fps" to ace.fps,
            "steps" to ace.steps,
            "guidance" to ace.guidance,
            "audio_reactive" to ace.audioReactive,
            "seed" to ace.seed,
        )
    }

    return GenerationPayload(model = selectedModel.id, parameters = params)
}
