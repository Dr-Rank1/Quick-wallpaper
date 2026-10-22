package com.rank.quickwallpaper.service

import android.annotation.SuppressLint
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.rank.quickwallpaper.data.repository.PhotoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class WallpaperShuffleTileService : TileService() {

    @Inject lateinit var photoRepository: PhotoRepository
    @Inject lateinit var wallpaperApplier: WallpaperApplier
    @Inject lateinit var notificationHelper: WallpaperNotificationHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartListening() {
        super.onStartListening()
        updateTileState(Tile.STATE_INACTIVE)
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        tile.state = Tile.STATE_UNAVAILABLE
        tile.updateTile()

        serviceScope.launch {
            try {
                val photoResult = photoRepository.getRandomPhoto()
                val photo = photoResult.getOrNull()
                if (photo != null) {
                    wallpaperApplier.apply(
                        imageUrl = photo.fullUrl,
                        target = WallpaperTarget.HOME,
                        fit = WallpaperFit.FILL
                    )

                    val bitmap = runCatching {
                        val request = ImageRequest.Builder(applicationContext)
                            .data(photo.smallUrl)
                            .size(512, 512)
                            .allowHardware(false)
                            .build()
                        val result = SingletonImageLoader.get(applicationContext).execute(request)
                        if (result is SuccessResult) result.image.toBitmap() else null
                    }.getOrNull()

                    notificationHelper.notify(
                        target = WallpaperTarget.HOME,
                        photographer = photo.photographer.name,
                        thumbnail = bitmap,
                        photoId = photo.id
                    )
                }
            } finally {
                updateTileState(Tile.STATE_INACTIVE)
            }
        }
    }

    private fun updateTileState(state: Int) {
        val tile = qsTile ?: return
        tile.state = state
        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
