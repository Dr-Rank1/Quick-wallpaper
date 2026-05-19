package com.rank.quickwallpaper.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.domain.model.ALL_CATEGORIES
import com.rank.quickwallpaper.domain.model.Category
import com.rank.quickwallpaper.domain.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: PhotoRepository
) : ViewModel() {

    val categories: List<Category> = ALL_CATEGORIES

    private val _selectedCategory = MutableStateFlow(ALL_CATEGORIES.first())
    val selectedCategory: StateFlow<Category> = _selectedCategory

    val photos: Flow<PagingData<Photo>> = _selectedCategory
        .flatMapLatest { category -> repository.getCategoryPhotos(category.query) }
        .cachedIn(viewModelScope)

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
    }
}
