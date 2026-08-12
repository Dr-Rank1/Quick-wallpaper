package com.rank.quickwallpaper.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rank.quickwallpaper.R
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int,
    val gradientStart: Color,
    val gradientEnd: Color,
    val iconTint: Color
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Outlined.PhotoLibrary,
        titleRes = R.string.onboarding_title_1,
        descRes = R.string.onboarding_desc_1,
        gradientStart = Color(0xFF0A0A0A),
        gradientEnd = Color(0xFF0D3B5E),
        iconTint = Color(0xFF00B4D8)
    ),
    OnboardingPage(
        icon = Icons.Outlined.Wallpaper,
        titleRes = R.string.onboarding_title_2,
        descRes = R.string.onboarding_desc_2,
        gradientStart = Color(0xFF0A0A0A),
        gradientEnd = Color(0xFF1A0533),
        iconTint = Color(0xFF9B59B6)
    ),
    OnboardingPage(
        icon = Icons.Outlined.Collections,
        titleRes = R.string.onboarding_title_3,
        descRes = R.string.onboarding_desc_3,
        gradientStart = Color(0xFF0A0A0A),
        gradientEnd = Color(0xFF0A2E1A),
        iconTint = Color(0xFF2ECC71)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { index ->
            val page = pages[index]
            val isActive = pagerState.currentPage == index
            val alpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.6f,
                animationSpec = tween(400),
                label = "page_alpha_$index"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to page.gradientStart,
                                0.6f to page.gradientEnd,
                                1f to page.gradientStart
                            )
                        )
                    )
                    .alpha(alpha)
                    .statusBarsPadding()
                    .padding(horizontal = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, bottom = 220.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(page.iconTint.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(MaterialTheme.shapes.large)
                                .background(page.iconTint.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = page.iconTint
                            )
                        }
                    }

                    Spacer(Modifier.height(48.dp))

                    Text(
                        text = stringResource(page.titleRes),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = stringResource(page.descRes),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.72f),
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(pages.size) { i ->
                    val width by animateDpAsState(
                        targetValue = if (i == pagerState.currentPage) 28.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "indicator_$i"
                    )
                    val color = if (i == pagerState.currentPage)
                        pages[pagerState.currentPage].iconTint
                    else Color.White.copy(alpha = 0.3f)

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            if (pagerState.currentPage < pages.size - 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onFinish) {
                        Text(
                            text = stringResource(R.string.skip),
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pages[pagerState.currentPage].iconTint
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.next),
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages.last().iconTint
                    )
                ) {
                    Text(
                        text = stringResource(R.string.get_started),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
