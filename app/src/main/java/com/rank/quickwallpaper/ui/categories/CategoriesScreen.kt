package com.rank.quickwallpaper.ui.categories

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.domain.model.Category
import com.rank.quickwallpaper.ui.components.ErrorState
import com.rank.quickwallpaper.ui.components.PhotoGridItem
import com.rank.quickwallpaper.ui.components.ShimmerGrid

private val categoryColors = mapOf(
    "nature"       to Color(0xFF2D6A4F),
    "architecture" to Color(0xFF5C4A8A),
    "abstract"     to Color(0xFFD64045),
    "animals"      to Color(0xFF6B4226),
    "travel"       to Color(0xFF1A6FA6),
    "minimal"      to Color(0xFF4A5568),
    "dark"         to Color(0xFF1A1A2E),
    "cars"         to Color(0xFF9B2335),
    "space"        to Color(0xFF0D1B2A),
    "kenya"        to Color(0xFF006600)
)

private val chipShape = RoundedCornerShape(50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onPhotoClick: (String) -> Unit,
    onCollectionsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val photos = viewModel.photos.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_categories)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 12.dp, end = 12.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onCollectionsClick != null) {
                        androidx.compose.material3.AssistChip(
                            onClick = onCollectionsClick,
                            label = { Text(stringResource(R.string.collections)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Collections,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.categories, key = { it.id }) { category ->
                        CategoryChip(
                            category = category,
                            selected = category.id == selectedCategory.id,
                            accentColor = categoryColors[category.id] ?: MaterialTheme.colorScheme.primary,
                            onClick = { viewModel.selectCategory(category) }
                        )
                    }
                }
            }

            when {
                photos.loadState.refresh is LoadState.Error -> {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        val e = (photos.loadState.refresh as LoadState.Error).error
                        ErrorState(
                            message = e.localizedMessage ?: stringResource(R.string.error_load),
                            onRetry = photos::retry,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                photos.loadState.refresh is LoadState.Loading -> {
                    item(span = StaggeredGridItemSpan.FullLine) { ShimmerGrid() }
                }
                else -> {
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

@Composable
private fun CategoryChip(
    category: Category,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) accentColor else accentColor.copy(alpha = 0.15f),
        animationSpec = tween(200),
        label = "chip_bg_${category.id}"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else accentColor,
        animationSpec = tween(200),
        label = "chip_text_${category.id}"
    )

    Box(
        modifier = Modifier
            .clip(chipShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${category.emoji}  ${category.displayName}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = textColor
        )
    }
}
