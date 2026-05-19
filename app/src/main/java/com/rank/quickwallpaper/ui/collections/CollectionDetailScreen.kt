package com.rank.quickwallpaper.ui.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.ui.components.ErrorState
import com.rank.quickwallpaper.ui.components.PhotoGridItem
import com.rank.quickwallpaper.ui.components.ShimmerGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    onBack: () -> Unit,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionDetailViewModel = hiltViewModel()
) {
    val photos = viewModel.photos.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.collectionTitle,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { padding ->
        when {
            photos.loadState.refresh is LoadState.Error -> {
                val e = (photos.loadState.refresh as LoadState.Error).error
                ErrorState(
                    message = e.localizedMessage ?: stringResource(R.string.error_load),
                    onRetry = photos::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            else -> {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 12.dp, end = 12.dp,
                        top = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 8.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (photos.loadState.refresh is LoadState.Loading) {
                        item(span = StaggeredGridItemSpan.FullLine) { ShimmerGrid() }
                    } else {
                        items(
                            count = photos.itemCount,
                            key = photos.itemKey { it.id }
                        ) { index ->
                            val photo = photos[index] ?: return@items
                            PhotoGridItem(photo = photo, onClick = { onPhotoClick(photo.id) })
                        }

                        if (photos.loadState.append is LoadState.Loading) {
                            item(span = StaggeredGridItemSpan.FullLine) { ShimmerGrid(2) }
                        }
                    }
                }
            }
        }
    }
}
