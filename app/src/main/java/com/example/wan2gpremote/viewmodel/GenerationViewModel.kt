package com.example.wan2gpremote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wan2gpremote.data.preferences.SettingsStore
import com.example.wan2gpremote.domain.AceStep15Options
import com.example.wan2gpremote.domain.FluxKlein9bOptions
import com.example.wan2gpremote.domain.GenerationSettings
import com.example.wan2gpremote.domain.Ltx2Options
import com.example.wan2gpremote.domain.ModelType
import com.example.wan2gpremote.domain.toPayload
import com.example.wan2gpremote.gallery.GenerationHistoryStore
import com.example.wan2gpremote.gallery.GenerationRecord
import com.example.wan2gpremote.gallery.MediaStorage
import com.example.wan2gpremote.gallery.OutputType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class GenerationUiState(
    val settings: GenerationSettings = GenerationSettings(),
    val serverIpDraft: String = "",
    val promptDraft: String = "",
    val mediaUriDraft: String = "",
    val serverJobIdDraft: String = "",
)

class GenerationViewModel(
    private val settingsStore: SettingsStore,
    private val historyStore: GenerationHistoryStore,
    private val mediaStorage: MediaStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerationUiState())
    val uiState: StateFlow<GenerationUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

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

    fun onPromptChanged(value: String) {
        _uiState.update { it.copy(promptDraft = value) }
    }

    fun onMediaUriChanged(value: String) {
        _uiState.update { it.copy(mediaUriDraft = value) }
    }

    fun onServerJobIdChanged(value: String) {
        _uiState.update { it.copy(serverJobIdDraft = value) }
    }

    fun saveServerIp() {
        viewModelScope.launch {
            settingsStore.updateServerIp(_uiState.value.serverIpDraft)
            _messages.emit("IP saved")
        }
    }

    fun selectModel(modelType: ModelType) {
        viewModelScope.launch { settingsStore.updateSelectedModel(modelType) }
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

    fun testConnectionPayload() {
        viewModelScope.launch {
            val payload = _uiState.value.settings.toPayload()
            _messages.emit("Ready to send ${payload.model} request to http://${_uiState.value.serverIpDraft}")
        }
    }

    fun completeGenerationAndSave() {
        viewModelScope.launch {
            val state = _uiState.value
            val model = state.settings.selectedModel
            val outputType = inferOutputType(model)
            val storedPath = mediaStorage.maybeDownloadToAppStorage(state.mediaUriDraft, outputType)
            val serverJobId = state.serverJobIdDraft.ifBlank { "job-${System.currentTimeMillis()}" }

            val record = GenerationRecord(
                id = UUID.randomUUID().toString(),
                model = model.label,
                mode = model.id,
                prompt = state.promptDraft,
                outputType = outputType,
                localPathOrUri = storedPath,
                createdAt = System.currentTimeMillis(),
                serverJobId = serverJobId,
            )
            historyStore.addRecord(record)
            _messages.emit("Saved generation to gallery")
            _uiState.update { it.copy(mediaUriDraft = "", serverJobIdDraft = "") }
        }
    }

    private fun inferOutputType(modelType: ModelType): OutputType {
        return when (modelType) {
            ModelType.FLUX_KLEIN_9B -> OutputType.IMAGE
            ModelType.LTX2, ModelType.ACE_STEP_15 -> OutputType.VIDEO
        }
    }

    class Factory(
        private val settingsStore: SettingsStore,
        private val historyStore: GenerationHistoryStore,
        private val mediaStorage: MediaStorage,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GenerationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GenerationViewModel(settingsStore, historyStore, mediaStorage) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
