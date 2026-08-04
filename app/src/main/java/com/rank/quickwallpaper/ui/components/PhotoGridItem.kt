package com.rank.quickwallpaper.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import com.rank.quickwallpaper.domain.model.Photo

private val cardShape = RoundedCornerShape(12.dp)

@Composable
fun PhotoGridItem(
    photo: Photo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val placeholderColor = remember(photo.color) {
        runCatching {
            Color(photo.color.orEmpty().toColorInt())
        }.getOrDefault(Color(0xFF1E1E1E))
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(200),
        label = "grid_item_scale"
    )

    val aspectRatioMod = if (photo.aspectRatio > 0)
        Modifier.aspectRatio(photo.aspectRatio.coerceIn(0.5f, 2.5f)) else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(cardShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .background(placeholderColor)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.smallUrl)
                .memoryCacheKey(photo.id)
                .diskCacheKey(photo.id)
                .crossfade(300)
                .scale(Scale.FILL)
                .build(),
            contentDescription = photo.description ?: photo.photographer.name,
            contentScale = ContentScale.Crop,
            loading = {
                ShimmerCard(
                    modifier = Modifier.fillMaxWidth().then(aspectRatioMod),
                    cornerRadius = 12
                )
            },
            modifier = Modifier.fillMaxWidth().then(aspectRatioMod)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.35f to Color.Black.copy(alpha = 0.15f),
                            0.65f to Color.Black.copy(alpha = 0.45f),
                            1f to Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
                .padding(start = 10.dp, end = 10.dp, top = 18.dp, bottom = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = photo.photographer.name.take(1),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = photo.photographer.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
