package com.rank.quickwallpaper.ui.categories

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.domain.model.Category
import com.rank.quickwallpaper.ui.components.BannerAdSlot
import com.rank.quickwallpaper.ui.components.ErrorState
import com.rank.quickwallpaper.ui.components.PhotoGridItem
import com.rank.quickwallpaper.ui.components.ShimmerGrid

private val EXPANDED_COLUMNS_WIDTH_DP = 840.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onPhotoClick: (String) -> Unit,
    onCollectionsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val photos = viewModel.photos.collectAsLazyPagingItems()

    val columnCount = with(LocalDensity.current) {
        if (LocalWindowInfo.current.containerSize.width.toDp() >= EXPANDED_COLUMNS_WIDTH_DP) 3 else 2
    }

    Scaffold(
        bottomBar = {
            BannerAdSlot(modifier = Modifier.padding(bottom = 8.dp))
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.nav_categories),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${viewModel.categories.size} ${stringResource(R.string.category_grid_mode)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // View Mode Toggle (Grid vs Stream)
                    IconButton(
                        onClick = viewModel::toggleViewMode,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (viewMode == CategoryViewMode.FEED) Icons.Filled.GridView else Icons.Filled.ViewStream,
                            contentDescription = stringResource(
                                if (viewMode == CategoryViewMode.FEED) R.string.category_grid_mode else R.string.category_feed_mode
                            ),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (onCollectionsClick != null) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            onClick = onCollectionsClick,
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Collections,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.collections_browse),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
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
        AnimatedContent(
            targetState = viewMode,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
            label = "categories_view_mode"
        ) { mode ->
            when (mode) {
                CategoryViewMode.ALL_GRID -> {
                    // All Categories Visual Gallery Mode
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnCount),
                        contentPadding = PaddingValues(
                            start = 14.dp,
                            end = 14.dp,
                            top = padding.calculateTopPadding() + 8.dp,
                            bottom = padding.calculateBottomPadding() + 16.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(viewModel.categories, key = { it.id }) { category ->
                            CategoryGridCard(
                                category = category,
                                isSelected = category.id == selectedCategory.id,
                                onClick = { viewModel.selectCategory(category) }
                            )
                        }
                    }
                }

                CategoryViewMode.FEED -> {
                    // Category Feed Mode with Hero & Wallpapers
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(columnCount),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding() + 12.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalItemSpacing = 10.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Category Horizontal Capsule Reel
                        item(span = StaggeredGridItemSpan.FullLine) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(viewModel.categories, key = { it.id }) { category ->
                                    CategoryCapsule(
                                        category = category,
                                        isSelected = category.id == selectedCategory.id,
                                        onClick = { viewModel.selectCategory(category) }
                                    )
                                }
                            }
                        }

                        // Category Hero Card
                        item(span = StaggeredGridItemSpan.FullLine) {
                            CategoryHeroCard(category = selectedCategory)
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
                                        isFavorite = photo.id in favoriteIds,
                                        onFavoriteClick = { viewModel.toggleFavorite(photo) },
                                        onClick = {
                                            viewModel.onPhotoSelected(activity) {
                                                onPhotoClick(photo.id)
                                            }
                                        }
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
        }
    }
}

/**
 * Top horizontal capsule selector for categories with smooth spring animation
 */
@Composable
private fun CategoryCapsule(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "capsule_scale_${category.id}"
    )

    val shape = RoundedCornerShape(24.dp)
    val startColor = Color(category.gradientStart)
    val endColor = Color(category.gradientEnd)

    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(listOf(startColor, endColor))
    } else {
        Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(shape)
            .background(backgroundBrush)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = shape
                    )
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = category.emoji,
                fontSize = 18.sp
            )
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

/**
 * Striking Hero Banner displayed above the category wallpaper grid
 */
@Composable
private fun CategoryHeroCard(category: Category) {
    val shape = RoundedCornerShape(22.dp)
    val startColor = Color(category.gradientStart)
    val endColor = Color(category.gradientEnd)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
            .clip(shape)
            .background(startColor)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = shape
            )
    ) {
        // Relevant photographic background picture like Collections page
        if (category.imageUrl.isNotBlank()) {
            AsyncImage(
                model = category.imageUrl,
                contentDescription = category.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Multi-stop cinematic gradient scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.82f),
                            Color.Black.copy(alpha = 0.65f),
                            endColor.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = category.emoji,
                        fontSize = 30.sp
                    )
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                // Highlight Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.22f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        Color.White.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = category.highlightBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (category.tagline.isNotBlank()) {
                Text(
                    text = category.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Rich category card displayed in the "All Themes" grid gallery view with
 * relevant photographic cover image, scrim overlay, and typography, just like in Collections page.
 */
@Composable
private fun CategoryGridCard(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val startColor = Color(category.gradientStart)

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "grid_card_scale_${category.id}"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .scale(scale)
            .clip(shape)
            .background(startColor)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                Color.White
                            )
                        ),
                        shape = shape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = shape
                    )
                }
            )
            .clickable(onClick = onClick)
    ) {
        // High quality relevant picture just like Collections page
        if (category.imageUrl.isNotBlank()) {
            AsyncImage(
                model = category.imageUrl,
                contentDescription = category.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Deep vertical gradient scrim to ensure title/text legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.92f)
                        ),
                        startY = 0f
                    )
                )
        )

        // Top Header: Badge & Selection Dot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.55f),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    Color.White.copy(alpha = 0.25f)
                )
            ) {
                Text(
                    text = category.highlightBadge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        // Bottom Info: Title, Tagline
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "${category.emoji} ${category.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (category.tagline.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = category.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
