package com.example.wan2gpremote.ui.modes

import androidx.compose.runtime.Composable
import com.example.wan2gpremote.domain.Ltx2Options
import com.example.wan2gpremote.ui.components.FloatSliderField
import com.example.wan2gpremote.ui.components.IntField
import com.example.wan2gpremote.ui.components.StringField

@Composable
fun LtxModeForm(options: Ltx2Options, onUpdate: (Ltx2Options) -> Unit) {
    IntField("Width", options.width) { onUpdate(options.copy(width = it)) }
    IntField("Height", options.height) { onUpdate(options.copy(height = it)) }
    IntField("Frames", options.frames) { onUpdate(options.copy(frames = it)) }
    IntField("Steps", options.steps) { onUpdate(options.copy(steps = it)) }
    FloatSliderField("CFG Scale", options.cfgScale, 1f..12f) { onUpdate(options.copy(cfgScale = it)) }
    StringField("Seed", options.seed) { onUpdate(options.copy(seed = it)) }
    StringField("Negative Prompt", options.negativePrompt) { onUpdate(options.copy(negativePrompt = it)) }
}
