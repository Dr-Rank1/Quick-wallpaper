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
        localPath: String? = null,
        blurRadius: Int = 0,
        dimPercent: Float = 0f
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val source = when {
                !localPath.isNullOrBlank() && File(localPath).exists() -> loadBitmapFromFile(localPath)
                else -> loadBitmap(imageUrl)
            }
            val prepared = prepareForScreen(source, fit)
            val finalBitmap = if (blurRadius > 0 || dimPercent > 0f) {
                applyBlurAndDim(prepared, blurRadius, dimPercent)
            } else {
                prepared
            }
            val manager = WallpaperManager.getInstance(context)

            when (target) {
                WallpaperTarget.HOME -> {
                    pinLockIfShared(manager)
                    manager.setBitmap(finalBitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                }
                WallpaperTarget.LOCK -> {
                    manager.setBitmap(finalBitmap, null, true, WallpaperManager.FLAG_LOCK)
                }
                WallpaperTarget.BOTH -> {
                    manager.setBitmap(
                        finalBitmap, null, true,
                        WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    )
                }
            }
            Unit
        }
    }

    /**
     * Applies fast bokeh blur and darkness overlay to the wallpaper bitmap.
     */
    private fun applyBlurAndDim(source: Bitmap, blurRadius: Int, dimPercent: Float): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        if (blurRadius > 0) {
            val factor = (blurRadius / 3).coerceIn(2, 8)
            val downW = (out.width / factor).coerceAtLeast(1)
            val downH = (out.height / factor).coerceAtLeast(1)
            val small = Bitmap.createScaledBitmap(out, downW, downH, true)
            val blurredSmall = stackBlur(small, (blurRadius / factor).coerceAtLeast(1))
            val scaledBack = Bitmap.createScaledBitmap(blurredSmall, out.width, out.height, true)
            Canvas(out).drawBitmap(scaledBack, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
            if (small != blurredSmall) small.recycle()
            blurredSmall.recycle()
            scaledBack.recycle()
        }
        if (dimPercent > 0f) {
            val canvas = Canvas(out)
            val paint = Paint().apply {
                color = android.graphics.Color.argb(
                    (dimPercent.coerceIn(0f, 0.8f) * 255).toInt(),
                    0, 0, 0
                )
            }
            canvas.drawRect(0f, 0f, out.width.toFloat(), out.height.toFloat(), paint)
        }
        return out
    }

    /**
     * Highly optimized stack blur for fast downscaled bitmap blurring.
     */
    private fun stackBlur(sentBitmap: Bitmap, radius: Int): Bitmap {
        if (radius < 1) return sentBitmap
        val w = sentBitmap.width
        val h = sentBitmap.height
        val pix = IntArray(w * h)
        sentBitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(maxOf(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (idx in 0 until 256 * divsum) {
            dv[idx] = idx / divsum
        }

        yi = 0
        yw = 0

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var rbs: Int
        val routsum = IntArray(1)
        val goutsum = IntArray(1)
        val boutsum = IntArray(1)
        val rinsum = IntArray(1)
        val ginsum = IntArray(1)
        val binsum = IntArray(1)

        for (row in 0 until h) {
            bsum = 0
            gsum = 0
            rsum = 0
            boutsum[0] = 0
            goutsum[0] = 0
            routsum[0] = 0
            binsum[0] = 0
            ginsum[0] = 0
            rinsum[0] = 0
            for (col in -radius..radius) {
                p = pix[yi + col.coerceIn(0, wm)]
                val sir = stack[col + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)
                rbs = radius + 1 - kotlin.math.abs(col)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (col > 0) {
                    rinsum[0] += sir[0]
                    ginsum[0] += sir[1]
                    binsum[0] += sir[2]
                } else {
                    routsum[0] += sir[0]
                    goutsum[0] += sir[1]
                    boutsum[0] += sir[2]
                }
            }
            stackpointer = radius

            for (col in 0 until w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum[0]
                gsum -= goutsum[0]
                bsum -= boutsum[0]

                stackstart = stackpointer - radius + div
                val sir = stack[stackstart % div]

                routsum[0] -= sir[0]
                goutsum[0] -= sir[1]
                boutsum[0] -= sir[2]

                if (row == 0) {
                    vmin[col] = (col + radius + 1).coerceAtMost(wm)
                }
                p = pix[yw + vmin[col]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)

                rinsum[0] += sir[0]
                ginsum[0] += sir[1]
                binsum[0] += sir[2]

                rsum += rinsum[0]
                gsum += ginsum[0]
                bsum += binsum[0]

                stackpointer = (stackpointer + 1) % div
                val sir2 = stack[stackpointer % div]

                routsum[0] += sir2[0]
                goutsum[0] += sir2[1]
                boutsum[0] += sir2[2]

                rinsum[0] -= sir2[0]
                ginsum[0] -= sir2[1]
                binsum[0] -= sir2[2]

                yi++
            }
            yw += w
        }

        for (col in 0 until w) {
            bsum = 0
            gsum = 0
            rsum = 0
            boutsum[0] = 0
            goutsum[0] = 0
            routsum[0] = 0
            binsum[0] = 0
            ginsum[0] = 0
            rinsum[0] = 0
            yp = -radius * w
            for (row in -radius..radius) {
                yi = maxOf(0, yp) + col
                val sir = stack[row + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = radius + 1 - kotlin.math.abs(row)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (row > 0) {
                    rinsum[0] += sir[0]
                    ginsum[0] += sir[1]
                    binsum[0] += sir[2]
                } else {
                    routsum[0] += sir[0]
                    goutsum[0] += sir[1]
                    boutsum[0] += sir[2]
                }
                if (row < hm) {
                    yp += w
                }
            }
            yi = col
            stackpointer = radius
            for (row in 0 until h) {
                pix[yi] = (0xff000000.toInt() and pix[yi]) or
                    (dv[rsum] shl 16) or
                    (dv[gsum] shl 8) or
                    dv[bsum]
                rsum -= routsum[0]
                gsum -= goutsum[0]
                bsum -= boutsum[0]

                stackstart = stackpointer - radius + div
                val sir = stack[stackstart % div]

                routsum[0] -= sir[0]
                goutsum[0] -= sir[1]
                boutsum[0] -= sir[2]

                if (col == 0) {
                    vmin[row] = (row + radius + 1).coerceAtMost(hm) * w
                }
                p = col + vmin[row]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum[0] += sir[0]
                ginsum[0] += sir[1]
                binsum[0] += sir[2]

                rsum += rinsum[0]
                gsum += ginsum[0]
                bsum += binsum[0]

                stackpointer = (stackpointer + 1) % div
                val sir2 = stack[stackpointer]

                routsum[0] += sir2[0]
                goutsum[0] += sir2[1]
                boutsum[0] += sir2[2]

                rinsum[0] -= sir2[0]
                ginsum[0] -= sir2[1]
                binsum[0] -= sir2[2]

                yi += w
            }
        }

        val result = sentBitmap.copy(sentBitmap.config ?: Bitmap.Config.ARGB_8888, true)
        result.setPixels(pix, 0, w, 0, 0, w, h)
        return result
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
