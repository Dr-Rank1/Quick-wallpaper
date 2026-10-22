package com.rank.quickwallpaper.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.domain.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: PhotoRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _selectedColor = MutableStateFlow<String?>(null)
    val selectedColor: StateFlow<String?> = _selectedColor

    val results: Flow<PagingData<Photo>> = combine(_query, _selectedColor) { q, c -> q to c }
        .debounce(400)
        .distinctUntilChanged()
        .filter { (q, c) -> q.isNotBlank() || c != null }
        .flatMapLatest { (q, c) ->
            val effectiveQuery = q.ifBlank { "wallpaper" }
            repository.searchPhotos(effectiveQuery, c)
        }
        .cachedIn(viewModelScope)

    fun onQueryChange(q: String) { _query.value = q }

    fun onColorSelected(color: String?) {
        _selectedColor.value = if (_selectedColor.value == color) null else color
    }

    fun clearQuery() {
        _query.value = ""
        _selectedColor.value = null
    }
}

