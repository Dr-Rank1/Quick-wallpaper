package com.rank.quickwallpaper.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import com.rank.quickwallpaper.BuildConfig
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.ui.theme.PaziaAccent
import com.rank.quickwallpaper.ui.theme.PaziaAccentLight
import com.rank.quickwallpaper.ui.theme.PaziaBorderLight
import com.rank.quickwallpaper.ui.theme.PaziaOverlayLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, coil.annotation.ExperimentalCoilApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.nav_settings),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SectionLabel(stringResource(R.string.settings_appearance))
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                ToggleRow(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.settings_dynamic_colors),
                    subtitle = stringResource(R.string.settings_dynamic_colors_desc),
                    checked = settings.useDynamicColors,
                    onCheckedChange = viewModel::setDynamicColors
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel(stringResource(R.string.settings_downloads))
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SettingIcon(icon = Icons.Outlined.Tune)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_quality),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.settings_quality_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DownloadQuality.entries.forEach { quality ->
                            val selected = settings.downloadQuality == quality
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setDownloadQuality(quality) },
                                label = {
                                    Text(
                                        when (quality) {
                                            DownloadQuality.MEDIUM -> stringResource(R.string.settings_quality_medium)
                                            DownloadQuality.LARGE -> stringResource(R.string.settings_quality_large)
                                            DownloadQuality.FULL -> stringResource(R.string.settings_quality_full)
                                        }
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PaziaAccent.copy(alpha = 0.15f),
                                    selectedLabelColor = PaziaAccent
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ClickableRow(
                    icon = Icons.Outlined.Delete,
                    title = stringResource(R.string.settings_clear_cache),
                    subtitle = stringResource(R.string.settings_clear_cache_desc),
                    onClick = {
                        scope.launch {
                            context.imageLoader.diskCache?.clear()
                            context.imageLoader.memoryCache?.clear()
                            snackbarHostState.showSnackbar(context.getString(R.string.settings_cache_cleared))
                        }
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel(stringResource(R.string.settings_support))
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                ClickableRow(
                    icon = Icons.Outlined.Grade,
                    title = stringResource(R.string.settings_rate),
                    subtitle = stringResource(R.string.settings_rate_desc),
                    trailingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=${context.packageName}")
                                )
                            )
                        }.onFailure {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                )
                            )
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ClickableRow(
                    icon = Icons.Outlined.Share,
                    title = stringResource(R.string.settings_share),
                    subtitle = stringResource(R.string.settings_share_desc),
                    onClick = {
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        context.getString(R.string.settings_share_text)
                                    )
                                },
                                null
                            )
                        )
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel(stringResource(R.string.settings_about))
            Spacer(Modifier.height(8.dp))
            SettingsCard {
                InfoRow(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.settings_version),
                    subtitle = BuildConfig.VERSION_NAME
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoRow(
                    icon = Icons.Outlined.CameraAlt,
                    title = stringResource(R.string.settings_attribution),
                    subtitle = stringResource(R.string.settings_attribution_desc)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ClickableRow(
                    icon = Icons.Outlined.Policy,
                    title = stringResource(R.string.pexels_license),
                    subtitle = null,
                    trailingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
                    iconTint = PaziaAccent,
                    titleColor = PaziaAccent,
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.pexels.com/license/"))
                        )
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            TextButton(
                onClick = {
                    scope.launch {
                        viewModel.setDarkTheme(true)
                        viewModel.setDynamicColors(true)
                        viewModel.setDownloadQuality(DownloadQuality.FULL)
                        snackbarHostState.showSnackbar(context.getString(R.string.settings_reset_done))
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.settings_reset))
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.pexels.com"))
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pazia v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " · ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.settings_powered_by),
                    style = MaterialTheme.typography.labelSmall,
                    color = PaziaAccent,
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingIcon(
    icon: ImageVector,
    tint: Color = PaziaAccent
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(PaziaOverlayLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, PaziaBorderLight, RoundedCornerShape(20.dp))
    ) {
        content()
    }
}

@Composable
private fun SectionLabel(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(PaziaAccent)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = PaziaAccent
        )
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon = icon)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PaziaAccent,
                checkedTrackColor = PaziaAccent.copy(alpha = 0.3f),
                checkedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun ClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
    iconTint: Color? = null,
    titleColor: Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon = icon, tint = iconTint ?: PaziaAccent)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor ?: MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        SettingIcon(icon = icon)
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
