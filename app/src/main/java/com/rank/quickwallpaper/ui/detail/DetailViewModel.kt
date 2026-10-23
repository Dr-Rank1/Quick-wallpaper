package com.rank.quickwallpaper.ui.detail

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.rank.quickwallpaper.ads.InterstitialAdManager
import com.rank.quickwallpaper.ads.RewardedAdManager
import com.rank.quickwallpaper.data.local.FavoriteOfflineCache
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.domain.model.Photo
import com.rank.quickwallpaper.download.DownloadEventBus
import com.rank.quickwallpaper.download.DownloadStatus
import com.rank.quickwallpaper.download.WallpaperDownloader
import com.rank.quickwallpaper.service.WallpaperApplier
import com.rank.quickwallpaper.service.WallpaperFit
import com.rank.quickwallpaper.service.WallpaperNotificationHelper
import com.rank.quickwallpaper.service.WallpaperTarget
import com.rank.quickwallpaper.ui.settings.DownloadQuality
import com.rank.quickwallpaper.ui.settings.SettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val photo: Photo, val isFavorite: Boolean) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

sealed class WallpaperState {
    object Idle : WallpaperState()
    object Applying : WallpaperState()
    object Success : WallpaperState()
    data class Error(val message: String) : WallpaperState()
}

sealed class DownloadState {
    object Idle : DownloadState()
    object InProgress : DownloadState()
    data class Success(val savedUri: Uri?) : DownloadState()
    object Failed : DownloadState()
    object AlreadyDownloaded : DownloadState()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PhotoRepository,
    private val wallpaperApplier: WallpaperApplier,
    private val dataStore: DataStore<Preferences>,
    private val wallpaperDownloader: WallpaperDownloader,
    private val interstitialAdManager: InterstitialAdManager,
    private val rewardedAdManager: RewardedAdManager,
    private val offlineCache: FavoriteOfflineCache,
    private val notificationHelper: WallpaperNotificationHelper,
    private val reviewManager: com.rank.quickwallpaper.service.InAppReviewManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val photoId: String = savedStateHandle["photoId"] ?: ""

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    private val _wallpaperState = MutableStateFlow<WallpaperState>(WallpaperState.Idle)
    val wallpaperState: StateFlow<WallpaperState> = _wallpaperState

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    private val _similar = MutableStateFlow<List<Photo>>(emptyList())
    val similar: StateFlow<List<Photo>> = _similar

    private val _showReviewPrompt = MutableStateFlow(false)
    val showReviewPrompt: StateFlow<Boolean> = _showReviewPrompt

    fun onRateApp(context: Context) {
        viewModelScope.launch {
            _showReviewPrompt.value = false
            reviewManager.openPlayStore(context)
        }
    }

    fun onDismissReview() {
        viewModelScope.launch {
            _showReviewPrompt.value = false
            reviewManager.dismissForNow()
        }
    }

    val downloadQuality: StateFlow<DownloadQuality> = dataStore.data.map { prefs ->
        prefs[SettingsViewModel.KEY_DOWNLOAD_QUALITY]
            ?.let { runCatching { DownloadQuality.valueOf(it) }.getOrDefault(DownloadQuality.FULL) }
            ?: DownloadQuality.FULL
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DownloadQuality.FULL)

    val wallpaperFit: StateFlow<WallpaperFit> = dataStore.data.map { prefs ->
        prefs[SettingsViewModel.KEY_WALLPAPER_FIT]
            ?.let { runCatching { WallpaperFit.valueOf(it) }.getOrDefault(WallpaperFit.FILL) }
            ?: WallpaperFit.FILL
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WallpaperFit.FILL)

    val adsRemoved: StateFlow<Boolean> = dataStore.data.map { prefs ->
        prefs[SettingsViewModel.KEY_ADS_REMOVED] ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var currentDownloadId: Long? = null
    private var loadJob: Job? = null
    private var favoriteJob: Job? = null

    val rewardedReady: StateFlow<Boolean> = rewardedAdManager.ready

    companion object {
        private val KEY_DOWNLOADED_IDS = stringSetPreferencesKey("downloaded_photo_ids")
    }

    init {
        loadPhoto()
        checkIfAlreadyDownloaded()
        observeDownloadCompletion()
        rewardedAdManager.load()
    }

    private fun checkIfAlreadyDownloaded() {
        if (photoId.isBlank()) return
        viewModelScope.launch {
            val ids = dataStore.data.map { it[KEY_DOWNLOADED_IDS] ?: emptySet() }.first()
            if (photoId in ids) _downloadState.value = DownloadState.AlreadyDownloaded
        }
    }

    private fun observeDownloadCompletion() {
        viewModelScope.launch {
            DownloadEventBus.events.collect { completedId ->
                val targetId = currentDownloadId ?: return@collect
                if (completedId != targetId) return@collect
                when (val status = wallpaperDownloader.queryStatus(completedId)) {
                    is DownloadStatus.Success -> {
                        persistDownloaded()
                        _downloadState.value = DownloadState.Success(status.uri)
                    }
                    is DownloadStatus.Failed -> _downloadState.value = DownloadState.Failed
                    else -> {}
                }
            }
        }
    }

    private fun persistDownloaded() {
        if (photoId.isBlank()) return
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_DOWNLOADED_IDS] = (prefs[KEY_DOWNLOADED_IDS] ?: emptySet()) + photoId
            }
        }
    }

    private fun loadPhoto() {
        if (photoId.isBlank()) {
            _uiState.value = DetailUiState.Error("Photo ID is missing")
            return
        }
        loadJob?.cancel()
        favoriteJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val result = repository.getPhoto(photoId)
            result.onSuccess { photo ->
                repository.recordHistory(photo)
                repository.getSimilarPhotos(photo).onSuccess { _similar.value = it }
                favoriteJob = viewModelScope.launch {
                    repository.isFavorite(photo.id).collectLatest { fav ->
                        _uiState.value = DetailUiState.Success(photo, fav)
                    }
                }
            }.onFailure { e ->
                _uiState.value = DetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun triggerDownload(activity: Activity, photo: Photo) {
        if (_downloadState.value is DownloadState.InProgress) return
        if (adsRemoved.value) {
            enqueueDownload(photo)
        } else {
            interstitialAdManager.maybeShow(activity, force = true) { enqueueDownload(photo) }
        }
    }

    private fun enqueueDownload(photo: Photo) {
        val url = when (downloadQuality.value) {
            DownloadQuality.MEDIUM -> photo.smallUrl
            DownloadQuality.LARGE -> photo.regularUrl
            DownloadQuality.FULL -> photo.fullUrl
        }
        _downloadState.value = DownloadState.InProgress
        val downloadId = try {
            wallpaperDownloader.enqueue(
                photoUrl = url,
                photoId = photo.id,
                photographer = photo.photographer.name
            )
        } catch (e: Exception) {
            _downloadState.value = DownloadState.Failed
            return
        }
        if (downloadId < 0) {
            _downloadState.value = DownloadState.Failed
            return
        }
        currentDownloadId = downloadId
        when (val status = wallpaperDownloader.queryStatus(downloadId)) {
            is DownloadStatus.Success -> {
                persistDownloaded()
                _downloadState.value = DownloadState.Success(status.uri)
            }
            is DownloadStatus.Failed -> _downloadState.value = DownloadState.Failed
            else -> {}
        }
    }

    fun retryDownload(activity: Activity, photo: Photo) {
        _downloadState.value = DownloadState.Idle
        triggerDownload(activity, photo)
    }

    fun acknowledgeSuccess() {
        _downloadState.value = DownloadState.AlreadyDownloaded
    }

    suspend fun findDownloadedUri(): Uri? = withContext(Dispatchers.IO) {
        runCatching {
            appContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                "${MediaStore.Images.Media.DISPLAY_NAME} = ?",
                arrayOf("pazia_$photoId.jpg"),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(idColumn)
                    )
                } else null
            }
        }.getOrNull()
    }

    fun toggleFavorite() {
        val state = _uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch {
            if (state.isFavorite) repository.removeFavorite(state.photo.id)
            else repository.addFavorite(state.photo)
        }
    }

    fun applyWallpaper(target: WallpaperTarget) {
        val state = _uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch {
            _wallpaperState.value = WallpaperState.Applying
            val local = offlineCache.pathFor(state.photo.id)
            wallpaperApplier.apply(
                imageUrl = state.photo.fullUrl,
                target = target,
                fit = wallpaperFit.value,
                localPath = local
            )
                .onSuccess {
                    _wallpaperState.value = WallpaperState.Success
                    // Post a rich notification with thumbnail so user knows even if they navigate away
                    postWallpaperSetNotification(state.photo, target)
                    if (reviewManager.recordWallpaperApplied()) {
                        _showReviewPrompt.value = true
                    }
                }
                .onFailure { e ->
                    _wallpaperState.value = WallpaperState.Error(e.message ?: "Failed to apply")
                }
        }
    }

    /** Fetches a small thumbnail bitmap and posts the wallpaper-set notification. */
    private fun postWallpaperSetNotification(photo: com.rank.quickwallpaper.domain.model.Photo, target: WallpaperTarget) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = runCatching {
                val request = coil3.request.ImageRequest.Builder(appContext)
                    .data(photo.smallUrl)
                    .size(512, 512)
                    .allowHardware(false)
                    .build()
                val result = coil3.SingletonImageLoader.get(appContext).execute(request)
                if (result is coil3.request.SuccessResult) result.image.toBitmap() else null
            }.getOrNull()

            notificationHelper.notify(
                target = target,
                photographer = photo.photographer.name,
                thumbnail = bitmap,
                photoId = photo.id
            )
        }
    }

    fun applyBothWithAd(activity: Activity) {
        if (_uiState.value !is DetailUiState.Success) return
        if (rewardedAdManager.ready.value) {
            rewardedAdManager.show(
                activity = activity,
                onRewarded = { applyWallpaper(WallpaperTarget.BOTH) },
                onDismissed = {
                    interstitialAdManager.maybeShow(
                        activity = activity,
                        force = true,
                        onDone = { applyWallpaper(WallpaperTarget.BOTH) }
                    )
                }
            )
        } else {
            interstitialAdManager.maybeShow(
                activity = activity,
                force = true,
                onDone = { applyWallpaper(WallpaperTarget.BOTH) }
            )
            rewardedAdManager.load()
        }
    }

    fun triggerRewardedSet(activity: Activity) {
        applyBothWithAd(activity)
    }

    fun unlockAdsRemoval(activity: Activity, onResult: (Boolean) -> Unit) {
        rewardedAdManager.show(
            activity = activity,
            onRewarded = {
                viewModelScope.launch {
                    dataStore.edit { it[SettingsViewModel.KEY_ADS_REMOVED] = true }
                    onResult(true)
                }
            },
            onDismissed = { onResult(false) }
        )
    }

    fun shareImage(activity: Activity) {
        val state = _uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) {
                runCatching {
                    val cacheDir = File(appContext.cacheDir, "share").also { it.mkdirs() }
                    val file = File(cacheDir, "pazia_${state.photo.id}.jpg")
                    val request = ImageRequest.Builder(appContext)
                        .data(state.photo.regularUrl)
                        .allowHardware(false)
                        .build()
                    val result = SingletonImageLoader.get(appContext).execute(request)
                    if (result !is SuccessResult) return@runCatching null
                    FileOutputStream(file).use { out ->
                        result.image.toBitmap().compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    FileProvider.getUriForFile(
                        appContext,
                        "${appContext.packageName}.fileprovider",
                        file
                    )
                }.getOrNull()
            }
            val send = Intent(Intent.ACTION_SEND).apply {
                if (uri != null) {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, state.photo.htmlLink)
                }
            }
            activity.startActivity(Intent.createChooser(send, null))
        }
    }

    fun maybeShowInterstitial(activity: Activity) {
        if (!adsRemoved.value) {
            interstitialAdManager.maybeShow(activity, force = true)
        }
    }

    fun resetWallpaperState() {
        _wallpaperState.value = WallpaperState.Idle
    }

    fun retry() = loadPhoto()
}
