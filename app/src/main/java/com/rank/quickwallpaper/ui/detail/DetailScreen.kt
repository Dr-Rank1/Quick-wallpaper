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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.service.WallpaperTarget
import com.rank.quickwallpaper.ui.components.ErrorState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wallpaperState by viewModel.wallpaperState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showWallpaperSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var showCheckmark by remember { mutableStateOf(false) }

    // Notification permission — request before first download on Android 13+
    val notifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    } else null

    // Wallpaper feedback
    LaunchedEffect(wallpaperState) {
        when (val state = wallpaperState) {
            is WallpaperState.Success -> {
                snackbarHost.showSnackbar(context.getString(R.string.wallpaper_set_success))
                viewModel.resetWallpaperState()
            }
            is WallpaperState.Error -> {
                snackbarHost.showSnackbar(state.message)
                viewModel.resetWallpaperState()
            }
            else -> {}
        }
    }

    // Download feedback — snackbar + haptic
    LaunchedEffect(downloadState) {
        when (val state = downloadState) {
            DownloadState.InProgress -> {
                snackbarHost.showSnackbar(
                    message = context.getString(R.string.download_in_progress),
                    duration = SnackbarDuration.Indefinite
                )
            }
            is DownloadState.Success -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                showCheckmark = true
                // Show snackbar independently so it outlasts the 2-second animation
                scope.launch {
                    val result = snackbarHost.showSnackbar(
                        message = context.getString(R.string.download_success),
                        actionLabel = context.getString(R.string.action_open),
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
                    message = context.getString(R.string.download_failed),
                    actionLabel = context.getString(R.string.action_retry),
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
                    CircularProgressIndicator()
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

                Box(modifier = Modifier.fillMaxSize()) {
                    var scale by remember { mutableStateOf(1f) }
                    var offsetX by remember { mutableStateOf(0f) }
                    var offsetY by remember { mutableStateOf(0f) }

                    val placeholderColor = runCatching {
                        Color(android.graphics.Color.parseColor(photo.color ?: "#1E1E1E"))
                    }.getOrDefault(Color(0xFF1E1E1E))

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photo.smallUrl)
                            .memoryCacheKey(photo.id)
                            .diskCacheKey(photo.id)
                            .size(Size.ORIGINAL)
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

                    // Back button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(0.5f), Color.Transparent)
                                )
                            )
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = Color.White
                            )
                        }
                    }

                    // Bottom action bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(0.85f))
                                )
                            )
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 32.dp,
                                bottom = 12.dp
                            )
                    ) {
                        TextButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(photo.photographer.profileUrl)
                                )
                                context.startActivity(intent)
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.photo_by, photo.photographer.name),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                textDecoration = TextDecoration.Underline
                            )
                        }

                        Text(
                            text = buildString {
                                append("${photo.width} × ${photo.height}")
                                photo.description?.takeIf { it.isNotBlank() }?.let {
                                    append("  ·  ")
                                    append(it.take(60))
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 2
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showWallpaperSheet = true },
                                modifier = Modifier.weight(1f),
                                enabled = wallpaperState !is WallpaperState.Applying
                            ) {
                                if (wallpaperState is WallpaperState.Applying) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Outlined.Wallpaper, null)
                                    Text(
                                        text = stringResource(R.string.set_wallpaper),
                                        modifier = Modifier.padding(start = 6.dp)
                                    )
                                }
                            }

                            // Download button — reflects download state
                            val isDownloaded = downloadState is DownloadState.AlreadyDownloaded
                            FilledIconButton(
                                onClick = {
                                    when (downloadState) {
                                        DownloadState.InProgress -> { /* already downloading */ }
                                        DownloadState.AlreadyDownloaded -> {
                                            scope.launch {
                                                val uri = viewModel.findDownloadedUri()
                                                val result = snackbarHost.showSnackbar(
                                                    message = context.getString(R.string.already_downloaded),
                                                    actionLabel = if (uri != null) context.getString(R.string.action_open) else null,
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
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (isDownloaded)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
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
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        "check" -> Icon(
                                            Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isDownloaded)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                Color.White
                                        )
                                        else -> Icon(
                                            Icons.Outlined.Download,
                                            contentDescription = stringResource(R.string.download)
                                        )
                                    }
                                }
                            }

                            FilledIconButton(
                                onClick = viewModel::toggleFavorite,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(
                                    imageVector = if (state.isFavorite)
                                        Icons.Outlined.Favorite
                                    else
                                        Icons.Outlined.FavoriteBorder,
                                    contentDescription = stringResource(R.string.favorite),
                                    tint = if (state.isFavorite) Color(0xFFFF4081) else Color.White
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
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Icon(Icons.Outlined.Share, stringResource(R.string.share))
                            }
                        }
                    }
                }

                if (showWallpaperSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showWallpaperSheet = false },
                        sheetState = sheetState,
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.set_wallpaper_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(4.dp))
        WallpaperTarget.entries.forEach { target ->
            OutlinedButton(
                onClick = { onSelect(target) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = when (target) {
                        WallpaperTarget.HOME -> stringResource(R.string.wallpaper_home)
                        WallpaperTarget.LOCK -> stringResource(R.string.wallpaper_lock)
                        WallpaperTarget.BOTH -> stringResource(R.string.wallpaper_both)
                    }
                )
            }
        }
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
