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
            "steps" to ltx2.steps,
            "cfg_scale" to ltx2.cfgScale,
            "seed" to ltx2.seed,
            "negative_prompt" to ltx2.negativePrompt,
        )

        ModelType.FLUX_KLEIN_9B -> mapOf(
            "width" to flux.width,
            "height" to flux.height,
            "steps" to flux.steps,
            "guidance" to flux.guidance,
            "sampler" to flux.sampler,
            "seed" to flux.seed,
            "safety_checker" to flux.safetyChecker,
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
