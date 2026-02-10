package com.example.wan2gpremote.ui.modes

import androidx.compose.runtime.Composable
import com.example.wan2gpremote.domain.FluxKlein9bOptions
import com.example.wan2gpremote.ui.components.BooleanChip
import com.example.wan2gpremote.ui.components.FloatSliderField
import com.example.wan2gpremote.ui.components.IntField
import com.example.wan2gpremote.ui.components.StringField

@Composable
fun FluxModeForm(options: FluxKlein9bOptions, onUpdate: (FluxKlein9bOptions) -> Unit) {
    IntField("Width", options.width) { onUpdate(options.copy(width = it)) }
    IntField("Height", options.height) { onUpdate(options.copy(height = it)) }
    IntField("Steps", options.steps) { onUpdate(options.copy(steps = it)) }
    FloatSliderField("Guidance", options.guidance, 1f..10f) { onUpdate(options.copy(guidance = it)) }
    StringField("Sampler", options.sampler) { onUpdate(options.copy(sampler = it)) }
    StringField("Seed", options.seed) { onUpdate(options.copy(seed = it)) }
    BooleanChip("Safety checker", options.safetyChecker) { onUpdate(options.copy(safetyChecker = it)) }
}
