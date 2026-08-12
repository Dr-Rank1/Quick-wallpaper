package com.rank.quickwallpaper.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.domain.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PhotoRepository
) : ViewModel() {

    val photos: Flow<PagingData<Photo>> = repository
        .getEditorialFeed()
        .cachedIn(viewModelScope)

    val favoriteIds: StateFlow<Set<String>> = repository.observeFavoriteIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _randomPhotoId = MutableStateFlow<String?>(null)
    val randomPhotoId: StateFlow<String?> = _randomPhotoId

    private val _shuffleError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val shuffleError: SharedFlow<String> = _shuffleError.asSharedFlow()

    private val _wallpaperOfDay = MutableStateFlow<Photo?>(null)
    val wallpaperOfDay: StateFlow<Photo?> = _wallpaperOfDay

    private val _history = MutableStateFlow<List<Photo>>(emptyList())
    val history: StateFlow<List<Photo>> = _history

    init {
        viewModelScope.launch {
            repository.getWallpaperOfTheDay().onSuccess { _wallpaperOfDay.value = it }
        }
        viewModelScope.launch {
            repository.observeHistory().collect { list ->
                _history.value = list.map { repository.historyToPhoto(it) }
            }
        }
    }

    fun shuffle() {
        viewModelScope.launch {
            repository.getRandomPhoto()
                .onSuccess { photo -> _randomPhotoId.value = photo.id }
                .onFailure { e ->
                    _shuffleError.tryEmit(e.message ?: "Couldn't load a random wallpaper")
                }
        }
    }

    fun onRandomPhotoNavigated() {
        _randomPhotoId.value = null
    }

    fun toggleFavorite(photo: Photo) {
        viewModelScope.launch { repository.toggleFavorite(photo) }
    }
}
