package com.rank.quickwallpaper.service

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class WallpaperTarget { HOME, LOCK, BOTH }

@Singleton
class WallpaperApplier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun apply(imageUrl: String, target: WallpaperTarget): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val newBitmap = loadBitmap(imageUrl)
                val manager = WallpaperManager.getInstance(context)

                when (target) {
                    WallpaperTarget.HOME -> {
                        pinLockIfShared(manager)
                        manager.setBitmap(newBitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    }
                    WallpaperTarget.LOCK -> {
                        manager.setBitmap(newBitmap, null, true, WallpaperManager.FLAG_LOCK)
                    }
                    WallpaperTarget.BOTH -> {
                        manager.setBitmap(
                            newBitmap, null, true,
                            WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                        )
                    }
                }
                Unit
            }
        }

    @SuppressLint("MissingPermission")
    private fun pinLockIfShared(manager: WallpaperManager) {
        try {
            val homeId = manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)
            val lockId = manager.getWallpaperId(WallpaperManager.FLAG_LOCK)
            if (homeId == lockId) {
                val currentDrawable = manager.drawable
                if (currentDrawable is BitmapDrawable && currentDrawable.bitmap != null) {
                    manager.setBitmap(
                        currentDrawable.bitmap,
                        null,
                        false,
                        WallpaperManager.FLAG_LOCK
                    )
                }
            }
        } catch (_: SecurityException) {
        }
    }

    private suspend fun loadBitmap(url: String): Bitmap {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = Coil.imageLoader(context).execute(request)
        if (result !is SuccessResult) throw IOException("Failed to load wallpaper image: ${url.take(80)}")
        return (result.drawable as BitmapDrawable).bitmap
    }
}
