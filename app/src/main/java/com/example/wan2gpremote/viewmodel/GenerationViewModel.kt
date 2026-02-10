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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GenerationUiState(
    val settings: GenerationSettings = GenerationSettings(),
    val serverIpDraft: String = ""
)

class GenerationViewModel(
    private val settingsStore: SettingsStore
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

    class Factory(private val settingsStore: SettingsStore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GenerationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GenerationViewModel(settingsStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
