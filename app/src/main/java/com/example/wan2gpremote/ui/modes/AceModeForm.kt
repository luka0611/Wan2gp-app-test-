package com.example.wan2gpremote.ui.modes

import androidx.compose.runtime.Composable
import com.example.wan2gpremote.domain.AceStep15Options
import com.example.wan2gpremote.ui.components.BooleanChip
import com.example.wan2gpremote.ui.components.FloatSliderField
import com.example.wan2gpremote.ui.components.IntField
import com.example.wan2gpremote.ui.components.StringField

@Composable
fun AceModeForm(options: AceStep15Options, onUpdate: (AceStep15Options) -> Unit) {
    IntField("Width", options.width) { onUpdate(options.copy(width = it)) }
    IntField("Height", options.height) { onUpdate(options.copy(height = it)) }
    IntField("Duration (s)", options.durationSeconds) { onUpdate(options.copy(durationSeconds = it)) }
    IntField("FPS", options.fps) { onUpdate(options.copy(fps = it)) }
    IntField("Steps", options.steps) { onUpdate(options.copy(steps = it)) }
    FloatSliderField("Guidance", options.guidance, 1f..12f) { onUpdate(options.copy(guidance = it)) }
    StringField("Seed", options.seed) { onUpdate(options.copy(seed = it)) }
    BooleanChip("Audio reactive", options.audioReactive) { onUpdate(options.copy(audioReactive = it)) }
}
