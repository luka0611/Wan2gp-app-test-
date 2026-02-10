package com.example.wan2gpremote.ui.modes

import androidx.compose.runtime.Composable
import com.example.wan2gpremote.domain.GenerationMode
import com.example.wan2gpremote.domain.ModeInputOptions
import com.example.wan2gpremote.ui.components.IntField
import com.example.wan2gpremote.ui.components.SectionHeader
import com.example.wan2gpremote.ui.components.StringField

@Composable
fun GenerationModeInputPanel(
    mode: GenerationMode,
    inputs: ModeInputOptions,
    onUpdate: (ModeInputOptions) -> Unit,
) {
    SectionHeader("Prompting")
    StringField("Prompt", inputs.prompt) { onUpdate(inputs.copy(prompt = it)) }
    StringField("Negative prompt", inputs.negativePrompt) { onUpdate(inputs.copy(negativePrompt = it)) }

    when (mode) {
        GenerationMode.TxtToImage,
        GenerationMode.TxtToVideo -> Unit

        GenerationMode.ImgToImg,
        GenerationMode.ImgToVideo -> {
            SectionHeader("Source image")
            StringField("Source image path / URI", inputs.sourceImagePath) {
                onUpdate(inputs.copy(sourceImagePath = it))
            }
        }

        GenerationMode.EditImage -> {
            SectionHeader("Image editing")
            StringField("Source image path / URI", inputs.sourceImagePath) {
                onUpdate(inputs.copy(sourceImagePath = it))
            }
            StringField("Mask path / URI", inputs.maskPath) {
                onUpdate(inputs.copy(maskPath = it))
            }
        }

        GenerationMode.ExtendVideo -> {
            SectionHeader("Extend settings")
            StringField("Source video path / URI", inputs.sourceVideoPath) {
                onUpdate(inputs.copy(sourceVideoPath = it))
            }
            IntField("Extend seconds", inputs.extendSeconds) {
                onUpdate(inputs.copy(extendSeconds = it.coerceIn(1, 60)))
            }
            IntField("Extend from frame (-1 = end)", inputs.extendFromFrame) {
                onUpdate(inputs.copy(extendFromFrame = it.coerceAtLeast(-1)))
            }
        }
    }
}
