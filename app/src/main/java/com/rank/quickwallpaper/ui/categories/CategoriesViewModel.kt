package com.rank.quickwallpaper.ui.categories

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rank.quickwallpaper.ads.InterstitialAdManager
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.domain.model.ALL_CATEGORIES
import com.rank.quickwallpaper.domain.model.Category
import com.rank.quickwallpaper.domain.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CategoryViewMode {
    FEED,
    ALL_GRID
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: PhotoRepository,
    private val interstitialAdManager: InterstitialAdManager
) : ViewModel() {

    val categories: List<Category> = ALL_CATEGORIES

    private val _selectedCategory = MutableStateFlow(ALL_CATEGORIES.first())
    val selectedCategory: StateFlow<Category> = _selectedCategory.asStateFlow()

    private val _viewMode = MutableStateFlow(CategoryViewMode.FEED)
    val viewMode: StateFlow<CategoryViewMode> = _viewMode.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = repository.observeFavoriteIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val photos: Flow<PagingData<Photo>> = _selectedCategory
        .flatMapLatest { category -> repository.getCategoryPhotos(category.query) }
        .cachedIn(viewModelScope)

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        _viewMode.value = CategoryViewMode.FEED
    }

    fun setViewMode(mode: CategoryViewMode) {
        _viewMode.value = mode
    }

    fun toggleViewMode() {
        _viewMode.value = if (_viewMode.value == CategoryViewMode.FEED) {
            CategoryViewMode.ALL_GRID
        } else {
            CategoryViewMode.FEED
        }
    }

    fun toggleFavorite(photo: Photo) {
        viewModelScope.launch {
            repository.toggleFavorite(photo)
        }
    }

    fun onPhotoSelected(activity: Activity?, onDone: () -> Unit) {
        if (activity != null) {
            interstitialAdManager.recordActionAndMaybeShow(activity, onDone)
        } else {
            onDone()
        }
    }
}
