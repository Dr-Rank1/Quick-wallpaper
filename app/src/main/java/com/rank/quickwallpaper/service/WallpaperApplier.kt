package com.rank.quickwallpaper.service

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    when (target) {
                        WallpaperTarget.HOME -> {
                            // Freeze lock screen before changing home.
                            // If no independent lock wallpaper exists, lock inherits home —
                            // pin the current home as an explicit lock so it stays unchanged.
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
                } else {
                    // Pre-N: only one wallpaper slot — can't separate home from lock
                    manager.setBitmap(newBitmap)
                }
                Unit
            }
        }

    /**
     * If home and lock share the same wallpaper ID (lock has no independent wallpaper),
     * snapshot the current home wallpaper and set it as an explicit lock wallpaper.
     * After this, changing home with FLAG_SYSTEM will no longer affect the lock screen.
     */
    private fun pinLockIfShared(manager: WallpaperManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        try {
            val homeId = manager.getWallpaperId(WallpaperManager.FLAG_SYSTEM)
            val lockId = manager.getWallpaperId(WallpaperManager.FLAG_LOCK)
            if (homeId == lockId) {
                // Lock is following home — freeze current home as an independent lock wallpaper
                val currentDrawable = manager.drawable
                if (currentDrawable is BitmapDrawable && currentDrawable.bitmap != null) {
                    manager.setBitmap(
                        currentDrawable.bitmap,
                        null,
                        false,   // don't allow backup for this interim snapshot
                        WallpaperManager.FLAG_LOCK
                    )
                }
            }
        } catch (e: SecurityException) {
            // On some devices reading the current wallpaper is restricted.
            // Fall through — the lock screen may still update but won't crash.
        }
    }

    private suspend fun loadBitmap(url: String): Bitmap {
        // Use the app-wide Coil singleton so disk cache is shared with the image grid
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = Coil.imageLoader(context).execute(request) as SuccessResult
        return (result.drawable as BitmapDrawable).bitmap
    }
}
