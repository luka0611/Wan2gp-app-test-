package com.example.wan2gpremote.ui.models

import androidx.compose.runtime.Composable
import com.example.wan2gpremote.domain.AceStep15Options
import com.example.wan2gpremote.domain.FluxKlein9bOptions
import com.example.wan2gpremote.domain.Ltx2Options
import com.example.wan2gpremote.ui.components.OptionCard
import com.example.wan2gpremote.ui.modes.AceModeForm
import com.example.wan2gpremote.ui.modes.FluxModeForm
import com.example.wan2gpremote.ui.modes.LtxModeForm

@Composable
fun LtxModelSection(options: Ltx2Options, onUpdate: (Ltx2Options) -> Unit) {
    OptionCard("LTX 2 options") {
        LtxModeForm(options = options, onUpdate = onUpdate)
    }
}

@Composable
fun FluxModelSection(options: FluxKlein9bOptions, onUpdate: (FluxKlein9bOptions) -> Unit) {
    OptionCard("Flux Klein 9b options") {
        FluxModeForm(options = options, onUpdate = onUpdate)
    }
}

@Composable
fun AceModelSection(options: AceStep15Options, onUpdate: (AceStep15Options) -> Unit) {
    OptionCard("Ace Step 1.5 options") {
        AceModeForm(options = options, onUpdate = onUpdate)
    }
}
