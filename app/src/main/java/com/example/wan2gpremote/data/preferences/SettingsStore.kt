package com.example.wan2gpremote.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
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
                steps = prefs[PrefKeys.LTX_STEPS] ?: 28,
                cfgScale = prefs[PrefKeys.LTX_CFG] ?: 3.5f,
                seed = prefs[PrefKeys.LTX_SEED].orEmpty(),
                negativePrompt = prefs[PrefKeys.LTX_NEGATIVE].orEmpty(),
            ),
            flux = FluxKlein9bOptions(
                width = prefs[PrefKeys.FLUX_WIDTH] ?: 1024,
                height = prefs[PrefKeys.FLUX_HEIGHT] ?: 1024,
                steps = prefs[PrefKeys.FLUX_STEPS] ?: 24,
                guidance = prefs[PrefKeys.FLUX_GUIDANCE] ?: 3.5f,
                seed = prefs[PrefKeys.FLUX_SEED].orEmpty(),
                sampler = prefs[PrefKeys.FLUX_SAMPLER].orEmpty().ifBlank { "euler" },
                safetyChecker = prefs[PrefKeys.FLUX_SAFETY] ?: true,
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
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.LTX_WIDTH] = options.width
            prefs[PrefKeys.LTX_HEIGHT] = options.height
            prefs[PrefKeys.LTX_FRAMES] = options.frames
            prefs[PrefKeys.LTX_STEPS] = options.steps
            prefs[PrefKeys.LTX_CFG] = options.cfgScale
            prefs[PrefKeys.LTX_SEED] = options.seed
            prefs[PrefKeys.LTX_NEGATIVE] = options.negativePrompt
        }
    }

    suspend fun updateFluxOptions(options: FluxKlein9bOptions) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.FLUX_WIDTH] = options.width
            prefs[PrefKeys.FLUX_HEIGHT] = options.height
            prefs[PrefKeys.FLUX_STEPS] = options.steps
            prefs[PrefKeys.FLUX_GUIDANCE] = options.guidance
            prefs[PrefKeys.FLUX_SAMPLER] = options.sampler
            prefs[PrefKeys.FLUX_SEED] = options.seed
            prefs[PrefKeys.FLUX_SAFETY] = options.safetyChecker
        }
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
    val LTX_STEPS = intPreferencesKey("ltx_steps")
    val LTX_CFG = floatPreferencesKey("ltx_cfg")
    val LTX_SEED = stringPreferencesKey("ltx_seed")
    val LTX_NEGATIVE = stringPreferencesKey("ltx_negative")

    val FLUX_WIDTH = intPreferencesKey("flux_width")
    val FLUX_HEIGHT = intPreferencesKey("flux_height")
    val FLUX_STEPS = intPreferencesKey("flux_steps")
    val FLUX_GUIDANCE = floatPreferencesKey("flux_guidance")
    val FLUX_SAMPLER = stringPreferencesKey("flux_sampler")
    val FLUX_SEED = stringPreferencesKey("flux_seed")
    val FLUX_SAFETY = booleanPreferencesKey("flux_safety")

    val ACE_WIDTH = intPreferencesKey("ace_width")
    val ACE_HEIGHT = intPreferencesKey("ace_height")
    val ACE_DURATION = intPreferencesKey("ace_duration")
    val ACE_FPS = intPreferencesKey("ace_fps")
    val ACE_STEPS = intPreferencesKey("ace_steps")
    val ACE_GUIDANCE = floatPreferencesKey("ace_guidance")
    val ACE_AUDIO = booleanPreferencesKey("ace_audio")
    val ACE_SEED = stringPreferencesKey("ace_seed")
}
