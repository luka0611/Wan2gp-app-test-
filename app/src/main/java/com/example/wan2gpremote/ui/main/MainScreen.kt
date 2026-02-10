package com.example.wan2gpremote.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wan2gpremote.data.preferences.SettingsStore
import com.example.wan2gpremote.data.storage.GalleryStorage
import com.example.wan2gpremote.domain.ModelType
import com.example.wan2gpremote.ui.components.OptionCard
import com.example.wan2gpremote.ui.components.StringField
import com.example.wan2gpremote.ui.models.AceModelSection
import com.example.wan2gpremote.ui.models.FluxModelSection
import com.example.wan2gpremote.ui.models.LtxModelSection
import com.example.wan2gpremote.viewmodel.GenerationRunState
import com.example.wan2gpremote.viewmodel.GenerationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val snackbars = remember { SnackbarHostState() }
    val vm: GenerationViewModel = viewModel(
        factory = GenerationViewModel.Factory(
            SettingsStore(context.applicationContext),
            GalleryStorage(context.applicationContext)
        )
    )
    val uiState by vm.uiState.collectAsState()

    LaunchedEffect(vm) {
        vm.messages.collect { snackbars.showSnackbar(it) }
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
            OptionCard("Main PC LAN IP") {
                StringField(
                    label = "Example: 192.168.1.25:7860",
                    value = uiState.serverIpDraft,
                    onValueChange = vm::onServerIpChanged
                )
                Button(onClick = vm::saveServerIp) { Text("Save IP") }
            }

            Text("Select Wan2GP Model", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModelType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.settings.selectedModel == type,
                        onClick = { vm.selectModel(type) },
                        label = { Text(type.label) }
                    )
                }
            }

            when (uiState.settings.selectedModel) {
                ModelType.LTX2 -> LtxModelSection(uiState.settings.ltx2, vm::updateLtxOptions)
                ModelType.FLUX_KLEIN_9B -> FluxModelSection(uiState.settings.flux, vm::updateFluxOptions)
                ModelType.ACE_STEP_15 -> AceModelSection(uiState.settings.ace, vm::updateAceOptions)
            }

            OptionCard("Generation Status") {
                when (val runState = uiState.runState) {
                    GenerationRunState.Idle -> Text("Idle")
                    is GenerationRunState.Submitting -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator()
                            Text(runState.message)
                        }
                    }
                    is GenerationRunState.Running -> {
                        Text("Running job: ${runState.jobId} (${runState.status})")
                        runState.progress?.let {
                            LinearProgressIndicator(progress = { it }, modifier = Modifier.fillMaxWidth())
                        } ?: CircularProgressIndicator()
                        Button(onClick = vm::cancelRunningJob) { Text("Cancel") }
                    }
                    is GenerationRunState.Completed -> {
                        Text("Completed job: ${runState.jobId}")
                        Text("Saved assets: ${runState.savedAssets.size}")
                    }
                    is GenerationRunState.Failed -> {
                        Text("Failed: ${runState.message}", color = MaterialTheme.colorScheme.error)
                        if (runState.canRetry) {
                            Button(onClick = vm::retryFailedJob) { Text("Retry") }
                        }
                    }
                }
            }

            Button(
                onClick = vm::submitGeneration,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start generation")
            }

            if (uiState.history.isNotEmpty()) {
                OptionCard("History") {
                    uiState.history.forEach { item ->
                        Text("${item.jobId}: ${item.savedAssets.size} asset(s)")
                    }
                }
            }
        }
    }
}
