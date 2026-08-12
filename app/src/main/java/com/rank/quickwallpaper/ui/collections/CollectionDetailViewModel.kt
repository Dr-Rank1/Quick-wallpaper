package com.rank.quickwallpaper.ui.collections

import androidx.lifecycle.SavedStateHandle
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
class CollectionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: PhotoRepository
) : ViewModel() {

    val collectionId: String = checkNotNull(savedStateHandle["collectionId"])
    // Navigation already URL-decodes path args; decoding again breaks titles that contain '%'.
    val collectionTitle: String = savedStateHandle["collectionTitle"] ?: ""

    val photos: Flow<PagingData<Photo>> = repository
        .getCollectionPhotos(collectionId)
        .cachedIn(viewModelScope)
}
