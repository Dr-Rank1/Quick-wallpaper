package com.rank.quickwallpaper.ui.categories

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.rank.quickwallpaper.ui.components.BannerAdSlot
import androidx.paging.compose.itemKey
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.domain.model.Category
import com.rank.quickwallpaper.ui.components.ErrorState
import com.rank.quickwallpaper.ui.components.PhotoGridItem
import com.rank.quickwallpaper.ui.components.ShimmerGrid

private val EXPANDED_COLUMNS_WIDTH_DP = 840.dp

private val categoryColors = mapOf(
    "nature"       to Color(0xFF059669),
    "architecture" to Color(0xFF7C3AED),
    "abstract"     to Color(0xFFDC2626),
    "animals"      to Color(0xFFD97706),
    "travel"       to Color(0xFF0284C7),
    "minimal"      to Color(0xFF6B7280),
    "dark"         to Color(0xFF1F2937),
    "cars"         to Color(0xFFBE123C),
    "space"        to Color(0xFF1E3A5F),
    "kenya"        to Color(0xFF059669),
    "street"       to Color(0xFFA16207),
    "food"         to Color(0xFFE11D48),
    "music"        to Color(0xFF9333EA),
    "technology"   to Color(0xFF0284C7),
    "people"       to Color(0xFFDB2777),
    "business"     to Color(0xFF2563EB),
    "health"       to Color(0xFF16A34A),
    "sports"       to Color(0xFFEA580C),
    "fashion"      to Color(0xFFC026D3),
    "film"         to Color(0xFF4F46E5)
)

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
    val columnCount = with(LocalDensity.current) {
        if (LocalWindowInfo.current.containerSize.width.toDp() >= EXPANDED_COLUMNS_WIDTH_DP) 3 else 2
    }

    Scaffold(
        bottomBar = {
            BannerAdSlot(modifier = Modifier.padding(vertical = 4.dp))
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_categories)) },
                actions = {
                    if (onCollectionsClick != null) {
                        TextButton(onClick = onCollectionsClick) {
                            Text(stringResource(R.string.collections_browse))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(columnCount),
            contentPadding = PaddingValues(
                start = 12.dp, end = 12.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.categories, key = { it.id }) { category ->
                        CategoryCard(
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
                        PhotoGridItem(
                            photo = photo,
                            onClick = { onPhotoClick(photo.id) }
                        )
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
private fun CategoryCard(
    category: Category,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val borderAlpha by animateColorAsState(
        targetValue = if (selected) accentColor else Color.Transparent,
        animationSpec = tween(300),
        label = "card_border_${category.id}"
    )
    val scale = if (selected) 1f else 0.96f

    Box(
        modifier = Modifier
            .width(120.dp)
            .height(152.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.55f),
                        accentColor.copy(alpha = 0.2f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(top = 14.dp, start = 12.dp, end = 12.dp, bottom = 12.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column {
            Text(
                text = category.emoji,
                fontSize = 28.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = Color.White.copy(alpha = if (selected) 1f else 0.8f)
            )
        }
        if (selected) {
            Text(
                text = "●",
                color = accentColor,
                fontSize = 8.sp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 4.dp)
            )
        }
    }
}
