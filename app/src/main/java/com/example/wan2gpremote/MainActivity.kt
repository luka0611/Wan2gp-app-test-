package com.example.wan2gpremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey

private val android.content.Context.dataStore by preferencesDataStore("wan2gp_settings")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Wan2GPRemoteApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Wan2GPRemoteApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbars = remember { SnackbarHostState() }

    var serverIp by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf(ModelType.LTX2) }
    var ltx2 by remember { mutableStateOf(Ltx2Options()) }
    var flux by remember { mutableStateOf(FluxKlein9bOptions()) }
    var ace by remember { mutableStateOf(AceStep15Options()) }

    LaunchedEffect(Unit) {
        val prefs = context.dataStore.data.first()
        serverIp = prefs[PrefKeys.SERVER_IP].orEmpty()
        selectedModel = ModelType.entries.firstOrNull { it.id == prefs[PrefKeys.MODEL] } ?: ModelType.LTX2
        ltx2 = Ltx2Options(
            width = prefs[PrefKeys.LTX_WIDTH] ?: 1024,
            height = prefs[PrefKeys.LTX_HEIGHT] ?: 576,
            frames = prefs[PrefKeys.LTX_FRAMES] ?: 73,
            steps = prefs[PrefKeys.LTX_STEPS] ?: 28,
            cfgScale = prefs[PrefKeys.LTX_CFG] ?: 3.5f,
            seed = prefs[PrefKeys.LTX_SEED].orEmpty(),
            negativePrompt = prefs[PrefKeys.LTX_NEGATIVE].orEmpty(),
        )
        flux = FluxKlein9bOptions(
            width = prefs[PrefKeys.FLUX_WIDTH] ?: 1024,
            height = prefs[PrefKeys.FLUX_HEIGHT] ?: 1024,
            steps = prefs[PrefKeys.FLUX_STEPS] ?: 24,
            guidance = prefs[PrefKeys.FLUX_GUIDANCE] ?: 3.5f,
            seed = prefs[PrefKeys.FLUX_SEED].orEmpty(),
            sampler = prefs[PrefKeys.FLUX_SAMPLER].orEmpty().ifBlank { "euler" },
            safetyChecker = prefs[PrefKeys.FLUX_SAFETY] ?: true,
        )
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
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Wan2GP LAN Remote") }) },
        snackbarHost = { SnackbarHost(hostState = snackbars) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Main PC LAN IP", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = serverIp,
                        onValueChange = { serverIp = it },
                        label = { Text("Example: 192.168.1.25:7860") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                context.dataStore.edit { it[PrefKeys.SERVER_IP] = serverIp.trim() }
                                snackbars.showSnackbar("IP saved")
                            }
                        }
                    ) { Text("Save IP") }
                }
            }

            Text("Select Wan2GP Model", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModelType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedModel == type,
                        onClick = {
                            selectedModel = type
                            scope.launch { context.dataStore.edit { it[PrefKeys.MODEL] = type.id } }
                        },
                        label = { Text(type.label) }
                    )
                }
            }

            when (selectedModel) {
                ModelType.LTX2 -> Ltx2Section(ltx2) {
                    ltx2 = it
                    scope.launch { context.dataStore.edit(it.toPreferencesUpdate()) }
                }
                ModelType.FLUX_KLEIN_9B -> FluxSection(flux) {
                    flux = it
                    scope.launch { context.dataStore.edit(it.toPreferencesUpdate()) }
                }
                ModelType.ACE_STEP_15 -> AceSection(ace) {
                    ace = it
                    scope.launch { context.dataStore.edit(it.toPreferencesUpdate()) }
                }
            }

            Button(onClick = {
                scope.launch {
                    snackbars.showSnackbar("Ready to send request to http://$serverIp")
                }
            }) {
                Text("Test connection payload")
            }
        }
    }
}

@Composable
private fun Ltx2Section(options: Ltx2Options, onUpdate: (Ltx2Options) -> Unit) {
    OptionCard("LTX 2 options") {
        IntField("Width", options.width) { onUpdate(options.copy(width = it)) }
        IntField("Height", options.height) { onUpdate(options.copy(height = it)) }
        IntField("Frames", options.frames) { onUpdate(options.copy(frames = it)) }
        IntField("Steps", options.steps) { onUpdate(options.copy(steps = it)) }
        FloatSliderField("CFG Scale", options.cfgScale, 1f..12f) { onUpdate(options.copy(cfgScale = it)) }
        StringField("Seed", options.seed) { onUpdate(options.copy(seed = it)) }
        StringField("Negative Prompt", options.negativePrompt) { onUpdate(options.copy(negativePrompt = it)) }
    }
}

@Composable
private fun FluxSection(options: FluxKlein9bOptions, onUpdate: (FluxKlein9bOptions) -> Unit) {
    OptionCard("Flux Klein 9b options") {
        IntField("Width", options.width) { onUpdate(options.copy(width = it)) }
        IntField("Height", options.height) { onUpdate(options.copy(height = it)) }
        IntField("Steps", options.steps) { onUpdate(options.copy(steps = it)) }
        FloatSliderField("Guidance", options.guidance, 1f..10f) { onUpdate(options.copy(guidance = it)) }
        StringField("Sampler", options.sampler) { onUpdate(options.copy(sampler = it)) }
        StringField("Seed", options.seed) { onUpdate(options.copy(seed = it)) }
        BooleanChip("Safety checker", options.safetyChecker) { onUpdate(options.copy(safetyChecker = it)) }
    }
}

@Composable
private fun AceSection(options: AceStep15Options, onUpdate: (AceStep15Options) -> Unit) {
    OptionCard("Ace Step 1.5 options") {
        IntField("Width", options.width) { onUpdate(options.copy(width = it)) }
        IntField("Height", options.height) { onUpdate(options.copy(height = it)) }
        IntField("Duration (s)", options.durationSeconds) { onUpdate(options.copy(durationSeconds = it)) }
        IntField("FPS", options.fps) { onUpdate(options.copy(fps = it)) }
        IntField("Steps", options.steps) { onUpdate(options.copy(steps = it)) }
        FloatSliderField("Guidance", options.guidance, 1f..12f) { onUpdate(options.copy(guidance = it)) }
        StringField("Seed", options.seed) { onUpdate(options.copy(seed = it)) }
        BooleanChip("Audio reactive", options.audioReactive) { onUpdate(options.copy(audioReactive = it)) }
    }
}

@Composable
private fun OptionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun IntField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValueChange(it.toIntOrNull() ?: value) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StringField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FloatSliderField(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column {
        Text("$label: ${"%.1f".format(value)}")
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

@Composable
private fun BooleanChip(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    FilterChip(
        selected = value,
        onClick = { onChange(!value) },
        label = { Text("$label: ${if (value) "On" else "Off"}") }
    )
}

enum class ModelType(val id: String, val label: String) {
    LTX2("ltx2", "LTX 2"),
    FLUX_KLEIN_9B("flux_klein_9b", "Flux Klein 9b"),
    ACE_STEP_15("ace_step_15", "Ace Step 1.5")
}

data class Ltx2Options(
    val width: Int = 1024,
    val height: Int = 576,
    val frames: Int = 73,
    val steps: Int = 28,
    val cfgScale: Float = 3.5f,
    val seed: String = "",
    val negativePrompt: String = "",
)

data class FluxKlein9bOptions(
    val width: Int = 1024,
    val height: Int = 1024,
    val steps: Int = 24,
    val guidance: Float = 3.5f,
    val sampler: String = "euler",
    val seed: String = "",
    val safetyChecker: Boolean = true,
)

data class AceStep15Options(
    val width: Int = 1280,
    val height: Int = 720,
    val durationSeconds: Int = 8,
    val fps: Int = 24,
    val steps: Int = 30,
    val guidance: Float = 4.0f,
    val audioReactive: Boolean = false,
    val seed: String = "",
)

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

private fun Ltx2Options.toPreferencesUpdate(): MutablePreferences.() -> Unit = {
    this[PrefKeys.LTX_WIDTH] = width
    this[PrefKeys.LTX_HEIGHT] = height
    this[PrefKeys.LTX_FRAMES] = frames
    this[PrefKeys.LTX_STEPS] = steps
    this[PrefKeys.LTX_CFG] = cfgScale
    this[PrefKeys.LTX_SEED] = seed
    this[PrefKeys.LTX_NEGATIVE] = negativePrompt
}

private fun FluxKlein9bOptions.toPreferencesUpdate(): MutablePreferences.() -> Unit = {
    this[PrefKeys.FLUX_WIDTH] = width
    this[PrefKeys.FLUX_HEIGHT] = height
    this[PrefKeys.FLUX_STEPS] = steps
    this[PrefKeys.FLUX_GUIDANCE] = guidance
    this[PrefKeys.FLUX_SAMPLER] = sampler
    this[PrefKeys.FLUX_SEED] = seed
    this[PrefKeys.FLUX_SAFETY] = safetyChecker
}

private fun AceStep15Options.toPreferencesUpdate(): MutablePreferences.() -> Unit = {
    this[PrefKeys.ACE_WIDTH] = width
    this[PrefKeys.ACE_HEIGHT] = height
    this[PrefKeys.ACE_DURATION] = durationSeconds
    this[PrefKeys.ACE_FPS] = fps
    this[PrefKeys.ACE_STEPS] = steps
    this[PrefKeys.ACE_GUIDANCE] = guidance
    this[PrefKeys.ACE_AUDIO] = audioReactive
    this[PrefKeys.ACE_SEED] = seed
}
