package com.rank.quickwallpaper.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.ui.components.ErrorState
import com.rank.quickwallpaper.ui.components.NativeAdSlot
import com.rank.quickwallpaper.ui.components.PhotoGridItem
import com.rank.quickwallpaper.ui.components.ShimmerGrid
import com.rank.quickwallpaper.ui.theme.PaziaAccent

private const val AD_INTERVAL = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPhotoClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onRandomPhoto: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val photos = viewModel.photos.collectAsLazyPagingItems()
    val isRefreshing = photos.loadState.refresh is LoadState.Loading
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val randomPhotoId by viewModel.randomPhotoId.collectAsState()

    LaunchedEffect(randomPhotoId) {
        randomPhotoId?.let { id ->
            onRandomPhoto(id)
            viewModel.onRandomPhotoNavigated()
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                onSearchClick = onSearchClick,
                onShuffleClick = viewModel::shuffle,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { photos.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                photos.loadState.refresh is LoadState.Error -> {
                    val error = (photos.loadState.refresh as LoadState.Error).error
                    ErrorState(
                        message = error.localizedMessage ?: stringResource(R.string.error_load),
                        onRetry = { photos.retry() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            start = 12.dp, end = 12.dp,
                            top = 4.dp, bottom = 16.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalItemSpacing = 10.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (photos.loadState.refresh is LoadState.Loading) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                ShimmerGrid(count = 6)
                            }
                        } else {
                            items(
                                count = photos.itemCount,
                                key = photos.itemKey { it.id }
                            ) { index ->
                                val photo = photos[index] ?: return@items
                                PhotoGridItem(
                                    photo = photo,
                                    onClick = { onPhotoClick(photo.id) }
                                )
                            }

                            if (photos.itemCount > 0 && photos.itemCount % AD_INTERVAL == 0 &&
                                photos.loadState.append !is LoadState.Loading) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    NativeAdSlot(Modifier.padding(vertical = 4.dp))
                                }
                            }

                            if (photos.loadState.append is LoadState.Loading) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Box(Modifier.padding(8.dp)) { ShimmerGrid(count = 2) }
                                }
                            }

                            if (photos.loadState.append is LoadState.Error) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    val error = (photos.loadState.append as LoadState.Error).error
                                    ErrorState(
                                        message = error.localizedMessage ?: stringResource(R.string.error_load),
                                        onRetry = { photos.retry() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onSearchClick: () -> Unit,
    onShuffleClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = "Pazia",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actions = {
            IconButton(onClick = onShuffleClick) {
                Icon(
                    Icons.Filled.Shuffle,
                    contentDescription = stringResource(R.string.random_wallpaper),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search_hint),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}
