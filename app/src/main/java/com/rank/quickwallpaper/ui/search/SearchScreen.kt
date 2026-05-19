package com.rank.quickwallpaper.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.ui.components.EmptyState
import com.rank.quickwallpaper.ui.components.ErrorState
import com.rank.quickwallpaper.ui.components.PhotoGridItem
import com.rank.quickwallpaper.ui.components.ShimmerGrid

@Composable
fun SearchScreen(
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results = viewModel.results.collectAsLazyPagingItems()
    val keyboard = LocalSoftwareKeyboardController.current

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearQuery) {
                            Icon(Icons.Outlined.Clear, stringResource(R.string.clear))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                shape = MaterialTheme.shapes.large
            )

            when {
                query.isBlank() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.search_prompt),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                results.loadState.refresh is LoadState.Loading -> ShimmerGrid()
                results.loadState.refresh is LoadState.Error -> {
                    val e = (results.loadState.refresh as LoadState.Error).error
                    ErrorState(
                        message = e.localizedMessage ?: stringResource(R.string.error_load),
                        onRetry = results::retry,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                results.itemCount == 0 && results.loadState.refresh is LoadState.NotLoading -> {
                    EmptyState(
                        message = stringResource(R.string.no_results, query),
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
                        items(
                            count = results.itemCount,
                            key = results.itemKey { it.id }
                        ) { index ->
                            val photo = results[index] ?: return@items
                            PhotoGridItem(photo = photo, onClick = { onPhotoClick(photo.id) })
                        }

                        if (results.loadState.append is LoadState.Error) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                val e = (results.loadState.append as LoadState.Error).error
                                ErrorState(
                                    message = e.localizedMessage ?: stringResource(R.string.error_load),
                                    onRetry = results::retry
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
