package com.rank.quickwallpaper.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val photos = viewModel.photos.collectAsLazyPagingItems()
    val isRefreshing = photos.loadState.refresh is LoadState.Loading
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            HomeTopBar(onSearchClick = onSearchClick, scrollBehavior = scrollBehavior)
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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
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
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Pazia",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Curated by Pexels",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            FilledIconButton(
                onClick = onSearchClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.search_hint)
                )
            }
            Spacer(Modifier.width(8.dp))
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}
