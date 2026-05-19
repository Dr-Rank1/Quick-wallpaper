package com.rank.quickwallpaper.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rank.quickwallpaper.data.local.entities.FavoriteEntity
import com.rank.quickwallpaper.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: PhotoRepository
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEntity>> = repository
        .observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(id: String) {
        viewModelScope.launch { repository.removeFavorite(id) }
    }
}
