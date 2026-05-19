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
    val collectionTitle: String = java.net.URLDecoder.decode(
        savedStateHandle["collectionTitle"] ?: "", "UTF-8"
    )

    val photos: Flow<PagingData<Photo>> = repository
        .getCollectionPhotos(collectionId)
        .cachedIn(viewModelScope)
}
