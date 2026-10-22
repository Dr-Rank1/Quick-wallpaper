package com.rank.quickwallpaper.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.domain.model.Photo
import com.rank.quickwallpaper.ui.components.BannerAdSlot
import com.rank.quickwallpaper.ui.components.ErrorState
import com.rank.quickwallpaper.ui.components.NativeAdSlot
import com.rank.quickwallpaper.ui.components.PhotoGridItem
import com.rank.quickwallpaper.ui.components.ShimmerGrid

private const val AD_INTERVAL = 12

private fun isAdSlot(position: Int): Boolean =
    (position + 1) % (AD_INTERVAL + 1) == 0

private fun photoIndex(position: Int): Int =
    position - position / (AD_INTERVAL + 1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPhotoClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onRandomPhoto: (String) -> Unit,
    modifier: Modifier = Modifier,
    adsRemoved: Boolean = false,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val photos = viewModel.photos.collectAsLazyPagingItems()
    val isRefreshing = photos.loadState.refresh is LoadState.Loading
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val randomPhotoId by viewModel.randomPhotoId.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val wallpaperOfDay by viewModel.wallpaperOfDay.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val shuffleFallback = stringResource(R.string.error_shuffle)

    LaunchedEffect(randomPhotoId) {
        randomPhotoId?.let { id ->
            onRandomPhoto(id)
            viewModel.onRandomPhotoNavigated()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.shuffleError.collect { message ->
            snackbarHostState.showSnackbar(message.ifBlank { shuffleFallback })
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { photos.refresh() },
                    modifier = Modifier.fillMaxSize()
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
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    HomeHeader(
                                        wallpaperOfDay = wallpaperOfDay,
                                        history = history,
                                        onPhotoClick = onPhotoClick
                                    )
                                }

                                if (photos.loadState.refresh is LoadState.Loading) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        ShimmerGrid(count = 6)
                                    }
                                } else {
                                    val photoCount = photos.itemCount
                                    val adCount = if (adsRemoved) 0 else photoCount / AD_INTERVAL
                                    val totalCount = photoCount + adCount
                                    items(
                                        count = totalCount,
                                        key = { position ->
                                            if (!adsRemoved && isAdSlot(position)) "ad_$position"
                                            else {
                                                val idx = if (adsRemoved) position else photoIndex(position)
                                                photos.peek(idx)?.id ?: "ph_$position"
                                            }
                                        },
                                        span = { position ->
                                            if (!adsRemoved && isAdSlot(position)) StaggeredGridItemSpan.FullLine
                                            else StaggeredGridItemSpan.SingleLane
                                        }
                                    ) { position ->
                                        if (!adsRemoved && isAdSlot(position)) {
                                            NativeAdSlot(Modifier.padding(vertical = 4.dp))
                                        } else {
                                            val idx = if (adsRemoved) position else photoIndex(position)
                                            val photo = photos[idx] ?: return@items
                                            PhotoGridItem(
                                                photo = photo,
                                                onClick = { onPhotoClick(photo.id) },
                                                isFavorite = photo.id in favoriteIds,
                                                onFavoriteClick = { viewModel.toggleFavorite(photo) }
                                            )
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

            if (!adsRemoved) {
                BannerAdSlot(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 72.dp)
                )
            } else {
                Spacer(Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun HomeHeader(
    wallpaperOfDay: Photo?,
    history: List<Photo>,
    onPhotoClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        wallpaperOfDay?.let { photo ->
            Text(
                text = stringResource(R.string.wallpaper_of_day),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onPhotoClick(photo.id) }
            ) {
                AsyncImage(
                    model = photo.regularUrl,
                    contentDescription = photo.description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )
                Text(
                    text = photo.description?.takeIf { it.isNotBlank() }
                        ?: photo.photographer.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        if (history.isNotEmpty()) {
            Text(
                text = stringResource(R.string.recently_viewed),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { it.id }) { photo ->
                    AsyncImage(
                        model = photo.thumbUrl.ifBlank { photo.smallUrl },
                        contentDescription = photo.description,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onPhotoClick(photo.id) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
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
                text = stringResource(R.string.app_name),
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
