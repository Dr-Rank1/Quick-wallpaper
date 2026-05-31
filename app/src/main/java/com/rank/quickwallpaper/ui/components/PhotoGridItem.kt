package com.rank.quickwallpaper.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.rank.quickwallpaper.domain.model.Photo

private val cardShape = RoundedCornerShape(14.dp)

@Composable
fun PhotoGridItem(
    photo: Photo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val placeholderColor = remember(photo.color) {
        runCatching {
            Color(android.graphics.Color.parseColor(photo.color ?: "#1E1E1E"))
        }.getOrDefault(Color(0xFF1E1E1E))
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(150),
        label = "grid_item_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .graphicsLayer {
                shadowElevation = if (isPressed) 4f else 2f
            }
            .clip(cardShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .background(placeholderColor)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.smallUrl)
                .memoryCacheKey(photo.id)
                .diskCacheKey(photo.id)
                .crossfade(250)
                .scale(Scale.FILL)
                .build(),
            contentDescription = photo.description ?: photo.photographer.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(photo.aspectRatio.coerceIn(0.5f, 2f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.5f to Color.Black.copy(alpha = 0.3f),
                            1f to Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = photo.photographer.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.95f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
