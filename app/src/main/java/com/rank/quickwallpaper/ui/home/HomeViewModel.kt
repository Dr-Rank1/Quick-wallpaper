package com.rank.quickwallpaper.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.domain.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: PhotoRepository
) : ViewModel() {

    val photos: Flow<PagingData<Photo>> = repository
        .getEditorialFeed()
        .cachedIn(viewModelScope)
}
