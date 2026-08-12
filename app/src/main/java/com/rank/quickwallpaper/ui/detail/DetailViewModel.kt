package com.rank.quickwallpaper.ui.detail

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rank.quickwallpaper.ads.InterstitialAdManager
import com.rank.quickwallpaper.ads.RewardedAdManager
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.domain.model.Photo
import com.rank.quickwallpaper.download.DownloadEventBus
import com.rank.quickwallpaper.download.DownloadStatus
import com.rank.quickwallpaper.download.WallpaperDownloader
import com.rank.quickwallpaper.service.WallpaperApplier
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
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val photoId: String = checkNotNull(savedStateHandle["photoId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    private val _wallpaperState = MutableStateFlow<WallpaperState>(WallpaperState.Idle)
    val wallpaperState: StateFlow<WallpaperState> = _wallpaperState

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState

    val downloadQuality: StateFlow<DownloadQuality> = dataStore.data.map { prefs ->
        prefs[SettingsViewModel.KEY_DOWNLOAD_QUALITY]
            ?.let { runCatching { DownloadQuality.valueOf(it) }.getOrDefault(DownloadQuality.FULL) }
            ?: DownloadQuality.FULL
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DownloadQuality.FULL)

    private var currentDownloadId: Long? = null
    private var loadJob: Job? = null

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
        viewModelScope.launch {
            val ids = dataStore.data.map { it[KEY_DOWNLOADED_IDS] ?: emptySet() }.first()
            if (photoId in ids) _downloadState.value = DownloadState.AlreadyDownloaded
        }
    }

    private fun observeDownloadCompletion() {
        viewModelScope.launch {
            DownloadEventBus.events.collect { completedId ->
                if (completedId != currentDownloadId) return@collect
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
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_DOWNLOADED_IDS] = (prefs[KEY_DOWNLOADED_IDS] ?: emptySet()) + photoId
            }
        }
    }

    private fun loadPhoto() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            repository.getPhoto(photoId)
                .onSuccess { photo ->
                    repository.isFavorite(photo.id).collectLatest { fav ->
                        _uiState.value = DetailUiState.Success(photo, fav)
                    }
                }
                .onFailure { e ->
                    _uiState.value = DetailUiState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun triggerDownload(activity: Activity, photo: Photo) {
        if (_downloadState.value is DownloadState.InProgress) return
        interstitialAdManager.maybeShow(activity) {
            enqueueDownload(photo)
        }
    }

    private fun enqueueDownload(photo: Photo) {
        val url = when (downloadQuality.value) {
            DownloadQuality.MEDIUM -> photo.smallUrl
            DownloadQuality.LARGE -> photo.regularUrl
            DownloadQuality.FULL -> photo.fullUrl
        }
        // Mark in-progress before enqueue so a fast completion broadcast can't be missed.
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
        // Reconcile race: completion may have arrived before currentDownloadId was set.
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
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(0)
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
            wallpaperApplier.apply(state.photo.fullUrl, target)
                .onSuccess { _wallpaperState.value = WallpaperState.Success }
                .onFailure { e ->
                    _wallpaperState.value = WallpaperState.Error(e.message ?: "Failed to apply")
                }
        }
    }

    fun triggerRewardedSet(activity: Activity) {
        if (_uiState.value !is DetailUiState.Success) return
        rewardedAdManager.show(
            activity = activity,
            onRewarded = { applyWallpaper(WallpaperTarget.BOTH) },
            onDismissed = {}
        )
    }

    fun resetWallpaperState() {
        _wallpaperState.value = WallpaperState.Idle
    }

    fun retry() = loadPhoto()
}
