package com.example.wan2gpremote.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.wan2gpremote.domain.AceStep15Options
import com.example.wan2gpremote.domain.FluxKlein9bOptions
import com.example.wan2gpremote.domain.GenerationSettings
import com.example.wan2gpremote.domain.Ltx2Options
import com.example.wan2gpremote.domain.ModelType
import com.example.wan2gpremote.domain.validated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("wan2gp_settings")

class SettingsStore(private val context: Context) {
    val settings: Flow<GenerationSettings> = context.dataStore.data.map { prefs ->
        GenerationSettings(
            serverIp = prefs[PrefKeys.SERVER_IP].orEmpty(),
            selectedModel = ModelType.entries.firstOrNull { it.id == prefs[PrefKeys.MODEL] } ?: ModelType.LTX2,
            ltx2 = Ltx2Options(
                width = prefs[PrefKeys.LTX_WIDTH] ?: 1024,
                height = prefs[PrefKeys.LTX_HEIGHT] ?: 576,
                frames = prefs[PrefKeys.LTX_FRAMES] ?: 73,
                fps = prefs[PrefKeys.LTX_FPS] ?: 24,
                steps = prefs[PrefKeys.LTX_STEPS] ?: 28,
                cfgScale = prefs[PrefKeys.LTX_CFG] ?: 3.5f,
                sampler = prefs[PrefKeys.LTX_SAMPLER].orEmpty().ifBlank { "euler" },
                scheduler = prefs[PrefKeys.LTX_SCHEDULER].orEmpty().ifBlank { "karras" },
                denoiseStrength = prefs[PrefKeys.LTX_DENOISE] ?: 1.0f,
                prompt = prefs[PrefKeys.LTX_PROMPT].orEmpty(),
                seed = prefs[PrefKeys.LTX_SEED].orEmpty(),
                randomizeSeed = prefs[PrefKeys.LTX_RANDOM_SEED] ?: true,
                negativePrompt = prefs[PrefKeys.LTX_NEGATIVE].orEmpty(),
                tiling = prefs[PrefKeys.LTX_TILING] ?: false,
                upscale = prefs[PrefKeys.LTX_UPSCALE] ?: false,
                upscaleFactor = prefs[PrefKeys.LTX_UPSCALE_FACTOR] ?: 1.0f,
            ),
            flux = FluxKlein9bOptions(
                width = prefs[PrefKeys.FLUX_WIDTH] ?: 1024,
                height = prefs[PrefKeys.FLUX_HEIGHT] ?: 1024,
                steps = prefs[PrefKeys.FLUX_STEPS] ?: 24,
                numImages = prefs[PrefKeys.FLUX_NUM_IMAGES] ?: 1,
                guidance = prefs[PrefKeys.FLUX_GUIDANCE] ?: 3.5f,
                seed = prefs[PrefKeys.FLUX_SEED].orEmpty(),
                sampler = prefs[PrefKeys.FLUX_SAMPLER].orEmpty().ifBlank { "euler" },
                scheduler = prefs[PrefKeys.FLUX_SCHEDULER].orEmpty().ifBlank { "karras" },
                strength = prefs[PrefKeys.FLUX_STRENGTH] ?: 1.0f,
                denoise = prefs[PrefKeys.FLUX_DENOISE] ?: 1.0f,
                prompt = prefs[PrefKeys.FLUX_PROMPT].orEmpty(),
                negativePrompt = prefs[PrefKeys.FLUX_NEGATIVE].orEmpty(),
                randomizeSeed = prefs[PrefKeys.FLUX_RANDOM_SEED] ?: true,
                safetyChecker = prefs[PrefKeys.FLUX_SAFETY] ?: true,
                tiling = prefs[PrefKeys.FLUX_TILING] ?: false,
                upscale = prefs[PrefKeys.FLUX_UPSCALE] ?: false,
                upscaleFactor = prefs[PrefKeys.FLUX_UPSCALE_FACTOR] ?: 1.0f,
            ),
            ace = AceStep15Options(
                width = prefs[PrefKeys.ACE_WIDTH] ?: 1280,
                height = prefs[PrefKeys.ACE_HEIGHT] ?: 720,
                durationSeconds = prefs[PrefKeys.ACE_DURATION] ?: 8,
                fps = prefs[PrefKeys.ACE_FPS] ?: 24,
                steps = prefs[PrefKeys.ACE_STEPS] ?: 30,
                guidance = prefs[PrefKeys.ACE_GUIDANCE] ?: 4.0f,
                audioReactive = prefs[PrefKeys.ACE_AUDIO] ?: false,
                seed = prefs[PrefKeys.ACE_SEED].orEmpty(),
            )
        )
    }

    suspend fun updateServerIp(serverIp: String) {
        context.dataStore.edit { prefs -> prefs[PrefKeys.SERVER_IP] = serverIp.trim() }
    }

    suspend fun updateSelectedModel(modelType: ModelType) {
        context.dataStore.edit { prefs -> prefs[PrefKeys.MODEL] = modelType.id }
    }

    suspend fun updateLtxOptions(options: Ltx2Options) {
        context.dataStore.edit { prefs -> options.validated().toPreferencesUpdate(prefs) }
    }

    suspend fun updateFluxOptions(options: FluxKlein9bOptions) {
        context.dataStore.edit { prefs -> options.validated().toPreferencesUpdate(prefs) }
    }

    suspend fun updateAceOptions(options: AceStep15Options) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.ACE_WIDTH] = options.width
            prefs[PrefKeys.ACE_HEIGHT] = options.height
            prefs[PrefKeys.ACE_DURATION] = options.durationSeconds
            prefs[PrefKeys.ACE_FPS] = options.fps
            prefs[PrefKeys.ACE_STEPS] = options.steps
            prefs[PrefKeys.ACE_GUIDANCE] = options.guidance
            prefs[PrefKeys.ACE_AUDIO] = options.audioReactive
            prefs[PrefKeys.ACE_SEED] = options.seed
        }
    }
}

private object PrefKeys {
    val SERVER_IP = stringPreferencesKey("server_ip")
    val MODEL = stringPreferencesKey("selected_model")

    val LTX_WIDTH = intPreferencesKey("ltx_width")
    val LTX_HEIGHT = intPreferencesKey("ltx_height")
    val LTX_FRAMES = intPreferencesKey("ltx_frames")
    val LTX_FPS = intPreferencesKey("ltx_fps")
    val LTX_STEPS = intPreferencesKey("ltx_steps")
    val LTX_CFG = floatPreferencesKey("ltx_cfg")
    val LTX_SAMPLER = stringPreferencesKey("ltx_sampler")
    val LTX_SCHEDULER = stringPreferencesKey("ltx_scheduler")
    val LTX_DENOISE = floatPreferencesKey("ltx_denoise")
    val LTX_PROMPT = stringPreferencesKey("ltx_prompt")
    val LTX_SEED = stringPreferencesKey("ltx_seed")
    val LTX_RANDOM_SEED = booleanPreferencesKey("ltx_random_seed")
    val LTX_NEGATIVE = stringPreferencesKey("ltx_negative")
    val LTX_TILING = booleanPreferencesKey("ltx_tiling")
    val LTX_UPSCALE = booleanPreferencesKey("ltx_upscale")
    val LTX_UPSCALE_FACTOR = floatPreferencesKey("ltx_upscale_factor")

    val FLUX_WIDTH = intPreferencesKey("flux_width")
    val FLUX_HEIGHT = intPreferencesKey("flux_height")
    val FLUX_STEPS = intPreferencesKey("flux_steps")
    val FLUX_NUM_IMAGES = intPreferencesKey("flux_num_images")
    val FLUX_GUIDANCE = floatPreferencesKey("flux_guidance")
    val FLUX_SAMPLER = stringPreferencesKey("flux_sampler")
    val FLUX_SCHEDULER = stringPreferencesKey("flux_scheduler")
    val FLUX_STRENGTH = floatPreferencesKey("flux_strength")
    val FLUX_DENOISE = floatPreferencesKey("flux_denoise")
    val FLUX_PROMPT = stringPreferencesKey("flux_prompt")
    val FLUX_NEGATIVE = stringPreferencesKey("flux_negative")
    val FLUX_SEED = stringPreferencesKey("flux_seed")
    val FLUX_RANDOM_SEED = booleanPreferencesKey("flux_random_seed")
    val FLUX_SAFETY = booleanPreferencesKey("flux_safety")
    val FLUX_TILING = booleanPreferencesKey("flux_tiling")
    val FLUX_UPSCALE = booleanPreferencesKey("flux_upscale")
    val FLUX_UPSCALE_FACTOR = floatPreferencesKey("flux_upscale_factor")

    val ACE_WIDTH = intPreferencesKey("ace_width")
    val ACE_HEIGHT = intPreferencesKey("ace_height")
    val ACE_DURATION = intPreferencesKey("ace_duration")
    val ACE_FPS = intPreferencesKey("ace_fps")
    val ACE_STEPS = intPreferencesKey("ace_steps")
    val ACE_GUIDANCE = floatPreferencesKey("ace_guidance")
    val ACE_AUDIO = booleanPreferencesKey("ace_audio")
    val ACE_SEED = stringPreferencesKey("ace_seed")
}

private fun Ltx2Options.toPreferencesUpdate(prefs: MutablePreferences) {
    prefs[PrefKeys.LTX_WIDTH] = width
    prefs[PrefKeys.LTX_HEIGHT] = height
    prefs[PrefKeys.LTX_FRAMES] = frames
    prefs[PrefKeys.LTX_FPS] = fps
    prefs[PrefKeys.LTX_STEPS] = steps
    prefs[PrefKeys.LTX_CFG] = cfgScale
    prefs[PrefKeys.LTX_SAMPLER] = sampler
    prefs[PrefKeys.LTX_SCHEDULER] = scheduler
    prefs[PrefKeys.LTX_DENOISE] = denoiseStrength
    prefs[PrefKeys.LTX_PROMPT] = prompt
    prefs[PrefKeys.LTX_SEED] = seed
    prefs[PrefKeys.LTX_RANDOM_SEED] = randomizeSeed
    prefs[PrefKeys.LTX_NEGATIVE] = negativePrompt
    prefs[PrefKeys.LTX_TILING] = tiling
    prefs[PrefKeys.LTX_UPSCALE] = upscale
    prefs[PrefKeys.LTX_UPSCALE_FACTOR] = upscaleFactor
}

private fun FluxKlein9bOptions.toPreferencesUpdate(prefs: MutablePreferences) {
    prefs[PrefKeys.FLUX_WIDTH] = width
    prefs[PrefKeys.FLUX_HEIGHT] = height
    prefs[PrefKeys.FLUX_STEPS] = steps
    prefs[PrefKeys.FLUX_NUM_IMAGES] = numImages
    prefs[PrefKeys.FLUX_GUIDANCE] = guidance
    prefs[PrefKeys.FLUX_SAMPLER] = sampler
    prefs[PrefKeys.FLUX_SCHEDULER] = scheduler
    prefs[PrefKeys.FLUX_STRENGTH] = strength
    prefs[PrefKeys.FLUX_DENOISE] = denoise
    prefs[PrefKeys.FLUX_PROMPT] = prompt
    prefs[PrefKeys.FLUX_NEGATIVE] = negativePrompt
    prefs[PrefKeys.FLUX_SEED] = seed
    prefs[PrefKeys.FLUX_RANDOM_SEED] = randomizeSeed
    prefs[PrefKeys.FLUX_SAFETY] = safetyChecker
    prefs[PrefKeys.FLUX_TILING] = tiling
    prefs[PrefKeys.FLUX_UPSCALE] = upscale
    prefs[PrefKeys.FLUX_UPSCALE_FACTOR] = upscaleFactor
}
