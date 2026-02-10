package com.example.wan2gpremote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wan2gpremote.data.preferences.SettingsStore
import com.example.wan2gpremote.data.repository.GenerationNetworkException
import com.example.wan2gpremote.data.repository.GenerationRepository
import com.example.wan2gpremote.data.storage.GalleryStorage
import com.example.wan2gpremote.domain.AceStep15Options
import com.example.wan2gpremote.domain.FluxKlein9bOptions
import com.example.wan2gpremote.domain.GenerationMode
import com.example.wan2gpremote.domain.GenerationSettings
import com.example.wan2gpremote.domain.Ltx2Options
import com.example.wan2gpremote.domain.ModelType
import com.example.wan2gpremote.domain.ModeInputOptions
import com.example.wan2gpremote.domain.supportedModesForModel
import com.example.wan2gpremote.domain.toPayload
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface GenerationRunState {
    data object Idle : GenerationRunState
    data class Submitting(val message: String = "Submitting job...") : GenerationRunState
    data class Running(val jobId: String, val progress: Float? = null, val status: String = "running") : GenerationRunState
    data class Completed(val jobId: String, val savedAssets: List<String>) : GenerationRunState
    data class Failed(val message: String, val canRetry: Boolean = true, val jobId: String? = null) : GenerationRunState
}

data class GenerationHistoryItem(
    val jobId: String,
    val savedAssets: List<String>,
)

data class GenerationUiState(
    val settings: GenerationSettings = GenerationSettings(),
    val serverIpDraft: String = "",
    val runState: GenerationRunState = GenerationRunState.Idle,
    val history: List<GenerationHistoryItem> = emptyList(),
)

class GenerationViewModel(
    private val settingsStore: SettingsStore,
    private val repository: GenerationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerationUiState())
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private var runningJob: Job? = null

    init {
        viewModelScope.launch {
            settingsStore.settings.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        settings = settings,
                        serverIpDraft = settings.serverIp
                    )
                }
            }
        }
    }

    fun onServerIpChanged(value: String) {
        _uiState.update { it.copy(serverIpDraft = value) }
    }

    fun saveServerIp() {
        viewModelScope.launch {
            settingsStore.updateServerIp(_uiState.value.serverIpDraft)
            _messages.emit("IP saved")
        }
    }

    fun selectModel(modelType: ModelType) {
        viewModelScope.launch {
            settingsStore.updateSelectedModel(modelType)
            val supportedModes = supportedModesForModel(modelType)
            val currentMode = _uiState.value.settings.selectedMode
            if (currentMode !in supportedModes) {
                settingsStore.updateSelectedMode(supportedModes.first())
            }
        }
    }

    fun selectMode(mode: GenerationMode) {
        viewModelScope.launch { settingsStore.updateSelectedMode(mode) }
    }

    fun updateModeInputs(mode: GenerationMode, inputs: ModeInputOptions) {
        viewModelScope.launch { settingsStore.updateModeInputs(mode, inputs) }
    }

    fun updateLtxOptions(options: Ltx2Options) {
        viewModelScope.launch { settingsStore.updateLtxOptions(options) }
    }

    fun updateFluxOptions(options: FluxKlein9bOptions) {
        viewModelScope.launch { settingsStore.updateFluxOptions(options) }
    }

    fun updateAceOptions(options: AceStep15Options) {
        viewModelScope.launch { settingsStore.updateAceOptions(options) }
    }

    fun submitGeneration() {
        runningJob?.cancel()
        runningJob = viewModelScope.launch {
            val state = _uiState.value
            val serverIp = state.serverIpDraft

            _uiState.update { it.copy(runState = GenerationRunState.Submitting()) }

            try {
                val payload = state.settings.toPayload()
                val jobId = repository.submitJob(serverIp, payload)
                _uiState.update { it.copy(runState = GenerationRunState.Running(jobId = jobId, status = "queued")) }

                val terminal = repository.pollUntilTerminal(serverIp, jobId)
                when (terminal.status.lowercase()) {
                    "completed" -> {
                        val savedAssets = repository.fetchAndPersistAssets(serverIp, jobId)
                        _uiState.update {
                            it.copy(
                                runState = GenerationRunState.Completed(jobId, savedAssets),
                                history = listOf(GenerationHistoryItem(jobId, savedAssets)) + it.history
                            )
                        }
                        _messages.emit("Generation completed and saved to gallery")
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                runState = GenerationRunState.Failed(
                                    message = terminal.error ?: "Job ${terminal.status}",
                                    jobId = jobId
                                )
                            )
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                _uiState.update {
                    it.copy(
                        runState = GenerationRunState.Failed(
                            message = e.message ?: "Invalid mode/model selection",
                            canRetry = false,
                        )
                    )
                }
            } catch (e: GenerationNetworkException) {
                _uiState.update {
                    it.copy(
                        runState = GenerationRunState.Failed(
                            message = e.message ?: "Network error",
                            canRetry = true,
                        )
                    )
                }
            }
        }
    }

    fun cancelRunningJob() {
        val current = _uiState.value.runState
        if (current !is GenerationRunState.Running) return

        viewModelScope.launch {
            try {
                repository.cancelJob(_uiState.value.serverIpDraft, current.jobId)
                _uiState.update { it.copy(runState = GenerationRunState.Idle) }
                _messages.emit("Job cancelled")
            } catch (e: GenerationNetworkException) {
                _uiState.update {
                    it.copy(runState = GenerationRunState.Failed(message = e.message ?: "Cancel failed", jobId = current.jobId))
                }
            }
        }
    }

    fun retryFailedJob() {
        val failed = _uiState.value.runState as? GenerationRunState.Failed ?: return
        val previousJobId = failed.jobId ?: run {
            submitGeneration()
            return
        }

        runningJob?.cancel()
        runningJob = viewModelScope.launch {
            _uiState.update { it.copy(runState = GenerationRunState.Submitting("Retrying job...")) }

            try {
                val retriedJobId = repository.retryJob(_uiState.value.serverIpDraft, previousJobId)
                _uiState.update { it.copy(runState = GenerationRunState.Running(jobId = retriedJobId)) }
                val terminal = repository.pollUntilTerminal(_uiState.value.serverIpDraft, retriedJobId)
                if (terminal.status.equals("completed", ignoreCase = true)) {
                    val savedAssets = repository.fetchAndPersistAssets(_uiState.value.serverIpDraft, retriedJobId)
                    _uiState.update {
                        it.copy(
                            runState = GenerationRunState.Completed(retriedJobId, savedAssets),
                            history = listOf(GenerationHistoryItem(retriedJobId, savedAssets)) + it.history,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(runState = GenerationRunState.Failed(terminal.error ?: "Retry failed", jobId = retriedJobId))
                    }
                }
            } catch (e: GenerationNetworkException) {
                _uiState.update { it.copy(runState = GenerationRunState.Failed(message = e.message ?: "Retry failed", jobId = previousJobId)) }
            }
        }
    }

    class Factory(private val settingsStore: SettingsStore, private val galleryStorage: GalleryStorage) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GenerationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GenerationViewModel(
                    settingsStore,
                    GenerationRepository(galleryStorage)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
