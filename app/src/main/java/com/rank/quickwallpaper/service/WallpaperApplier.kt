package com.rank.quickwallpaper.service

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class WallpaperTarget { HOME, LOCK, BOTH }

enum class WallpaperFit {
    FILL,   // center-crop to screen
    FIT,    // letterbox / contain
    STRETCH // stretch to fill
}

@Singleton
class WallpaperApplier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun apply(
        imageUrl: String,
        target: WallpaperTarget,
        fit: WallpaperFit = WallpaperFit.FILL,
        localPath: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val source = when {
                !localPath.isNullOrBlank() && File(localPath).exists() -> loadBitmapFromFile(localPath)
                else -> loadBitmap(imageUrl)
            }
            val prepared = prepareForScreen(source, fit)
            val manager = WallpaperManager.getInstance(context)

            when (target) {
                WallpaperTarget.HOME -> {
                    pinLockIfShared(manager)
                    manager.setBitmap(prepared, null, true, WallpaperManager.FLAG_SYSTEM)
                }
                WallpaperTarget.LOCK -> {
                    manager.setBitmap(prepared, null, true, WallpaperManager.FLAG_LOCK)
                }
                WallpaperTarget.BOTH -> {
                    manager.setBitmap(
                        prepared, null, true,
                        WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    )
                }
            }
            Unit
        }
    }

    private fun prepareForScreen(source: Bitmap, fit: WallpaperFit): Bitmap {
        val (sw, sh) = screenSize()
        if (sw <= 0 || sh <= 0) return source
        return when (fit) {
            WallpaperFit.STRETCH -> source.scale(sw, sh)
            WallpaperFit.FIT -> containBitmap(source, sw, sh)
            WallpaperFit.FILL -> coverBitmap(source, sw, sh)
        }
    }

    private fun coverBitmap(source: Bitmap, sw: Int, sh: Int): Bitmap {
        val scale = maxOf(sw.toFloat() / source.width, sh.toFloat() / source.height)
        val tw = (source.width * scale).toInt().coerceAtLeast(1)
        val th = (source.height * scale).toInt().coerceAtLeast(1)
        val scaled = source.scale(tw, th)
        val x = ((tw - sw) / 2).coerceAtLeast(0)
        val y = ((th - sh) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(scaled, x, y, sw.coerceAtMost(scaled.width), sh.coerceAtMost(scaled.height))
        if (scaled != source && scaled != cropped) {
            scaled.recycle()
        }
        return cropped
    }

    private fun containBitmap(source: Bitmap, sw: Int, sh: Int): Bitmap {
        val scale = minOf(sw.toFloat() / source.width, sh.toFloat() / source.height)
        val tw = (source.width * scale).toInt().coerceAtLeast(1)
        val th = (source.height * scale).toInt().coerceAtLeast(1)
        val scaled = source.scale(tw, th)
        val out = createBitmap(sw, sh, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(android.graphics.Color.BLACK)
        val left = (sw - tw) / 2f
        val top = (sh - th) / 2f
        canvas.drawBitmap(scaled, left, top, Paint(Paint.FILTER_BITMAP_FLAG))
        if (scaled != source && scaled != out) {
            scaled.recycle()
        }
        return out
    }

    private fun screenSize(): Pair<Int, Int> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            bounds.width() to bounds.height()
        } else {
            @Suppress("DEPRECATION")
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealMetrics(metrics)
            metrics.widthPixels to metrics.heightPixels
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

    private fun loadBitmapFromFile(path: String): Bitmap {
        return android.graphics.BitmapFactory.decodeFile(path)
            ?: throw IOException("Failed to decode cached wallpaper: $path")
    }

    private suspend fun loadBitmap(url: String): Bitmap {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = SingletonImageLoader.get(context).execute(request)
        if (result !is SuccessResult) throw IOException("Failed to load wallpaper image: ${url.take(80)}")
        return result.image.toBitmap()
    }
}
