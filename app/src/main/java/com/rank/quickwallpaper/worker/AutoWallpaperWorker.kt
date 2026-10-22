package com.rank.quickwallpaper.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.service.WallpaperApplier
import com.rank.quickwallpaper.service.WallpaperFit
import com.rank.quickwallpaper.service.WallpaperTarget
import com.rank.quickwallpaper.ui.settings.SettingsViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class AutoWallpaperWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: PhotoRepository,
    private val wallpaperApplier: WallpaperApplier,
    private val dataStore: DataStore<Preferences>
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val favorites = repository.getFavoriteEntities()
        val (imageUrl, local) = if (favorites.isNotEmpty()) {
            val entity = favorites.random()
            Pair<String, String?>(entity.fullUrl, entity.localCachedPath.takeIf { it.isNotBlank() })
        } else {
            val fallback = repository.getRandomPhoto().getOrNull() ?: return Result.retry()
            val url = fallback.fullUrl.ifBlank { fallback.regularUrl }
            Pair<String, String?>(url, null)
        }

        val prefs = dataStore.data.first()
        val fit = prefs[SettingsViewModel.KEY_WALLPAPER_FIT]
            ?.let { runCatching { WallpaperFit.valueOf(it) }.getOrDefault(WallpaperFit.FILL) }
            ?: WallpaperFit.FILL

        return wallpaperApplier.apply(
            imageUrl = imageUrl,
            target = WallpaperTarget.HOME,
            fit = fit,
            localPath = local
        ).fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}
