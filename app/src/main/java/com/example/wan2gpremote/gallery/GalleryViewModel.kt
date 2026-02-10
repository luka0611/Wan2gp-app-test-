package com.example.wan2gpremote.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class GalleryViewModel(
    historyStore: GenerationHistoryStore
) : ViewModel() {

    val records: StateFlow<List<GenerationRecord>> = historyStore.records
        .map { it.sortedByDescending { record -> record.createdAt } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    class Factory(private val historyStore: GenerationHistoryStore) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GalleryViewModel(historyStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
