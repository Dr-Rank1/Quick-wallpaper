package com.rank.quickwallpaper.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import coil3.size.Size
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.service.WallpaperTarget
import com.rank.quickwallpaper.ui.components.ErrorState
import com.rank.quickwallpaper.ui.components.PhotoGridItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wallpaperState by viewModel.wallpaperState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val rewardedReady by viewModel.rewardedReady.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showWallpaperSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var showCheckmark by remember { mutableStateOf(false) }

    val wallpaperSetSuccessMsg = stringResource(R.string.wallpaper_set_success)
    val downloadInProgressMsg = stringResource(R.string.download_in_progress)
    val downloadSuccessMsg = stringResource(R.string.download_success)
    val downloadFailedMsg = stringResource(R.string.download_failed)
    val actionOpenLabel = stringResource(R.string.action_open)
    val actionRetryLabel = stringResource(R.string.action_retry)
    val alreadyDownloadedMsg = stringResource(R.string.already_downloaded)

    val notifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    } else null

    LaunchedEffect(wallpaperState) {
        when (val state = wallpaperState) {
            is WallpaperState.Success -> {
                snackbarHost.showSnackbar(wallpaperSetSuccessMsg)
                viewModel.resetWallpaperState()
            }
            is WallpaperState.Error -> {
                snackbarHost.showSnackbar(state.message)
                viewModel.resetWallpaperState()
            }
            else -> {}
        }
    }

    LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            DownloadState.InProgress -> {
                snackbarHost.showSnackbar(
                    message = downloadInProgressMsg,
                    duration = SnackbarDuration.Indefinite
                )
            }
            is DownloadState.Success -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                showCheckmark = true
                scope.launch {
                    val result = snackbarHost.showSnackbar(
                        message = downloadSuccessMsg,
                        actionLabel = actionOpenLabel,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        state.savedUri?.let { openSavedImage(context, it) }
                    }
                }
                delay(2_000)
                showCheckmark = false
                viewModel.acknowledgeSuccess()
            }
            DownloadState.Failed -> {
                val result = snackbarHost.showSnackbar(
                    message = downloadFailedMsg,
                    actionLabel = actionRetryLabel,
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    (uiState as? DetailUiState.Success)?.photo?.let { photo ->
                        val activity = context as? ComponentActivity ?: return@LaunchedEffect
                        viewModel.retryDownload(activity, photo)
                    }
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        modifier = modifier
    ) { padding ->
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is DetailUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = viewModel::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            is DetailUiState.Success -> {
                val photo = state.photo
                var heroReady by remember { mutableStateOf(false) }
                val heroAlpha by animateFloatAsState(
                    targetValue = if (heroReady) 1f else 0f,
                    animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    label = "hero_alpha"
                )
                LaunchedEffect(Unit) { heroReady = true }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            alpha = heroAlpha,
                            scaleX = 0.92f + 0.08f * heroAlpha,
                            scaleY = 0.92f + 0.08f * heroAlpha
                        )
                ) {
                    var scale by remember { mutableFloatStateOf(1f) }
                    var offsetX by remember { mutableFloatStateOf(0f) }
                    var offsetY by remember { mutableFloatStateOf(0f) }

                    val placeholderColor = runCatching {
                        Color(photo.color.orEmpty().toColorInt())
                    }.getOrDefault(Color(0xFF1E1E1E))

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photo.smallUrl)
                            .memoryCacheKey(photo.id)
                            .diskCacheKey(photo.id)
                            .size(800, 800)
                            .scale(Scale.FIT)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(placeholderColor)
                    )

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photo.regularUrl)
                            .memoryCacheKey("${photo.id}_hq")
                            .diskCacheKey("${photo.id}_hq")
                            .size(Size.ORIGINAL)
                            .scale(Scale.FIT)
                            .crossfade(400)
                            .build(),
                        contentDescription = photo.description,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    if (scale > 1f) {
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                                )
                            )
                            .padding(top = 8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = Color.White
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 40.dp,
                                bottom = 16.dp
                            )
                    ) {
                        TextButton(
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            photo.photographer.profileUrl.toUri()
                                        )
                                    )
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.photo_by, photo.photographer.name),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                textDecoration = TextDecoration.Underline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = buildString {
                                append("${photo.width} × ${photo.height}")
                                photo.description?.takeIf { it.isNotBlank() }?.let {
                                    append("  ·  ")
                                    append(it.take(60))
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.55f),
                            maxLines = 2
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showWallpaperSheet = true },
                                modifier = Modifier.weight(1f).height(50.dp),
                                enabled = wallpaperState !is WallpaperState.Applying,
                                shape = MaterialTheme.shapes.medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (wallpaperState is WallpaperState.Applying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.Wallpaper,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.set_wallpaper),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            val isDownloaded = downloadState is DownloadState.AlreadyDownloaded
                            FilledIconButton(
                                onClick = {
                                    when (downloadState) {
                                        DownloadState.InProgress -> {}
                                        DownloadState.AlreadyDownloaded -> {
                                            scope.launch {
                                                val uri = viewModel.findDownloadedUri()
                                                val result = snackbarHost.showSnackbar(
                                                    message = alreadyDownloadedMsg,
                                                    actionLabel = if (uri != null) actionOpenLabel else null,
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed && uri != null) {
                                                    openSavedImage(context, uri)
                                                }
                                            }
                                        }
                                        else -> {
                                            if (notifPermission != null && !notifPermission.status.isGranted) {
                                                notifPermission.launchPermissionRequest()
                                            }
                                            val activity = context as? ComponentActivity ?: return@FilledIconButton
                                            viewModel.triggerDownload(activity, photo)
                                        }
                                    }
                                },
                                enabled = downloadState !is DownloadState.InProgress,
                                modifier = Modifier.size(50.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isDownloaded) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.15f)
                                )
                            ) {
                                AnimatedContent(
                                    targetState = when {
                                        downloadState is DownloadState.InProgress -> "progress"
                                        showCheckmark || isDownloaded -> "check"
                                        else -> "download"
                                    },
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "download_icon"
                                ) { target ->
                                    when (target) {
                                        "progress" -> CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        "check" -> Icon(
                                            Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        else -> Icon(
                                            Icons.Outlined.Download,
                                            contentDescription = stringResource(R.string.download),
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            FilledIconButton(
                                onClick = viewModel::toggleFavorite,
                                modifier = Modifier.size(50.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (state.isFavorite) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else Color.White.copy(alpha = 0.15f)
                                )
                            ) {
                                Icon(
                                    imageVector = if (state.isFavorite) Icons.Outlined.Favorite
                                    else Icons.Outlined.FavoriteBorder,
                                    contentDescription = stringResource(R.string.favorite),
                                    tint = if (state.isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            FilledIconButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, photo.htmlLink)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, null))
                                },
                                modifier = Modifier.size(50.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f)
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.Share,
                                    contentDescription = stringResource(R.string.share),
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val activity = context as? ComponentActivity ?: return@Button
                                viewModel.triggerRewardedSet(activity)
                            },
                            enabled = rewardedReady,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Filled.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.rewarded_set_both),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (showWallpaperSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showWallpaperSheet = false },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surface,
                        dragHandle = { BottomSheetDefaults.DragHandle() }
                    ) {
                        WallpaperTargetSheet(
                            onSelect = { target ->
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showWallpaperSheet = false
                                    viewModel.applyWallpaper(target)
                                }
                            },
                            onDismiss = { showWallpaperSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WallpaperTargetSheet(
    onSelect: (WallpaperTarget) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.set_wallpaper_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        WallpaperTarget.entries.forEach { target ->
            Button(
                onClick = { onSelect(target) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = when (target) {
                        WallpaperTarget.HOME -> stringResource(R.string.wallpaper_home)
                        WallpaperTarget.LOCK -> stringResource(R.string.wallpaper_lock)
                        WallpaperTarget.BOTH -> stringResource(R.string.wallpaper_both)
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.cancel))
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun openSavedImage(context: android.content.Context, uri: Uri) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "image/jpeg")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_VIEW).apply { type = "image/*" },
                null
            )
        )
    }
}
