package com.rank.quickwallpaper.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rank.quickwallpaper.ui.theme.PaziaShimmerBase
import com.rank.quickwallpaper.ui.theme.PaziaShimmerHighlight

private val cardShape = RoundedCornerShape(14.dp)

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 200.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        PaziaShimmerBase,
                        PaziaShimmerHighlight,
                        PaziaShimmerBase
                    ),
                    start = Offset(progress - 300f, progress - 300f),
                    end = Offset(progress, progress)
                ),
                shape = cardShape
            )
    )
}

@Composable
fun ShimmerGrid(count: Int = 6) {
    val heights = listOf(220.dp, 160.dp, 260.dp, 190.dp, 240.dp, 155.dp, 205.dp, 175.dp)
    repeat(count) { i ->
        ShimmerBox(height = heights[i % heights.size])
    }
}
