package com.rank.quickwallpaper.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SkinSelectorRow(
    selected: AppSkinId,
    onSelect: (AppSkinId) -> Unit,
    modifier: Modifier = Modifier,
    skins: List<PaziaSkin> = AllPaziaSkins
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        skins.forEach { skin ->
            SkinPreviewCard(
                skin = skin,
                selected = skin.id == selected,
                onClick = { onSelect(skin.id) }
            )
        }
    }
}

@Composable
private fun SkinPreviewCard(
    skin: PaziaSkin,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(92.dp)
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(92.dp, 72.dp)
                .clip(shape)
                .background(skin.previewBackground)
                .then(
                    if (selected) {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = shape
                        )
                    } else {
                        Modifier.border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = shape
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(10.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(skin.previewAccent)
            )
            Text(
                text = "Aa",
                color = skin.previewOnBackground,
                style = skin.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .height(4.dp)
                    .width(28.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(skin.previewAccent)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = skin.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
