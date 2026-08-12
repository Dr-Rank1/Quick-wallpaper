package com.rank.quickwallpaper.data.local

import android.content.Context
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteOfflineCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dir: File
        get() = File(context.filesDir, "favorite_cache").also { if (!it.exists()) it.mkdirs() }

    suspend fun cachePhoto(photoId: String, imageUrl: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val outFile = File(dir, "$photoId.jpg")
            if (outFile.exists() && outFile.length() > 0L) return@runCatching outFile.absolutePath

            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            val result = SingletonImageLoader.get(context).execute(request)
            if (result !is SuccessResult) return@runCatching null
            val bitmap = result.image.toBitmap()
            FileOutputStream(outFile).use { fos ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, fos)
            }
            outFile.absolutePath
        }.getOrNull()
    }

    fun delete(photoId: String) {
        File(dir, "$photoId.jpg").delete()
    }

    fun pathFor(photoId: String): String? {
        val f = File(dir, "$photoId.jpg")
        return f.takeIf { it.exists() && it.length() > 0L }?.absolutePath
    }
}
