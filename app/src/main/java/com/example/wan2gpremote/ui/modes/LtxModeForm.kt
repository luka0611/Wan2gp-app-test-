package com.example.wan2gpremote.ui.modes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.wan2gpremote.domain.Ltx2Options
import com.example.wan2gpremote.domain.validated
import com.example.wan2gpremote.ui.components.BooleanChip
import com.example.wan2gpremote.ui.components.DropdownField
import com.example.wan2gpremote.ui.components.FloatSliderField
import com.example.wan2gpremote.ui.components.IntField
import com.example.wan2gpremote.ui.components.SectionHeader
import com.example.wan2gpremote.ui.components.StringField

@Composable
fun LtxModeForm(options: Ltx2Options, onUpdate: (Ltx2Options) -> Unit) {
    val samplers = remember { listOf("euler", "euler_a", "heun", "dpmpp_2m", "ddim") }
    val schedulers = remember { listOf("karras", "normal", "sgm_uniform", "simple") }

    SectionHeader("Resolution")
    IntField("Width (256-2048)", options.width) { onUpdate(options.copy(width = it).validated()) }
    IntField("Height (256-2048)", options.height) { onUpdate(options.copy(height = it).validated()) }
    IntField("Frames (8-240)", options.frames) { onUpdate(options.copy(frames = it).validated()) }
    IntField("FPS (1-60)", options.fps) { onUpdate(options.copy(fps = it).validated()) }

    SectionHeader("Sampling")
    IntField("Steps (1-80)", options.steps) { onUpdate(options.copy(steps = it).validated()) }
    FloatSliderField("CFG Scale (1.0-20.0)", options.cfgScale, 1f..20f) {
        onUpdate(options.copy(cfgScale = it).validated())
    }
    DropdownField("Sampler", options.sampler, samplers) { onUpdate(options.copy(sampler = it).validated()) }
    DropdownField("Scheduler", options.scheduler, schedulers) { onUpdate(options.copy(scheduler = it).validated()) }
    FloatSliderField("Denoise Strength (0.0-1.0)", options.denoiseStrength, 0f..1f) {
        onUpdate(options.copy(denoiseStrength = it).validated())
    }

    SectionHeader("Prompting")
    StringField("Prompt (optional)", options.prompt) { onUpdate(options.copy(prompt = it).validated()) }
    StringField("Negative Prompt (optional)", options.negativePrompt) {
        onUpdate(options.copy(negativePrompt = it).validated())
    }
    StringField("Seed (optional integer)", options.seed) { onUpdate(options.copy(seed = it).validated()) }
    BooleanChip("Randomize Seed", options.randomizeSeed) { onUpdate(options.copy(randomizeSeed = it).validated()) }

    SectionHeader("Advanced")
    BooleanChip("Tiling", options.tiling) { onUpdate(options.copy(tiling = it).validated()) }
    BooleanChip("Upscale", options.upscale) { onUpdate(options.copy(upscale = it).validated()) }
    FloatSliderField("Upscale Factor (1.0-4.0)", options.upscaleFactor, 1f..4f) {
        onUpdate(options.copy(upscaleFactor = it).validated())
    }
}
