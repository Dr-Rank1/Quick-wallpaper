package com.rank.quickwallpaper.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.domain.model.Collection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CollectionsUiState {
    object Loading : CollectionsUiState()
    data class Success(val collections: List<Collection>) : CollectionsUiState()
    data class Error(val message: String) : CollectionsUiState()
}

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val repository: PhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectionsUiState>(CollectionsUiState.Loading)
    val uiState: StateFlow<CollectionsUiState> = _uiState

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CollectionsUiState.Loading
            repository.getCollections()
                .onSuccess { _uiState.value = CollectionsUiState.Success(it) }
                .onFailure { _uiState.value = CollectionsUiState.Error(it.message ?: "Error") }
        }
    }
}
