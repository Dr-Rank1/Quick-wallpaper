package com.rank.quickwallpaper.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class InProgress(val progress: Int) : DownloadStatus()
    data class Success(val uri: Uri?) : DownloadStatus()
    object Failed : DownloadStatus()
}

@Singleton
class WallpaperDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun enqueue(photoUrl: String, photoId: String, photographer: String): Long {
        val filename = "pazia_$photoId.jpg"
        val request = DownloadManager.Request(Uri.parse(photoUrl))
            .setTitle("Pazia wallpaper")
            .setDescription("Photo by $photographer")
            .setMimeType("image/jpeg")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Pazia/$filename")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        return downloadManager.enqueue(request)
    }

    fun queryStatus(downloadId: Long): DownloadStatus {
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query)?.use { cursor ->
            if (!cursor.moveToFirst()) return DownloadStatus.Idle
            val status = cursor.getInt(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            )
            val total = cursor.getLong(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            ).coerceAtLeast(1L)
            val soFar = cursor.getLong(
                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            )
            val progress = ((soFar * 100f) / total).toInt().coerceIn(0, 100)
            return when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.Success(
                    runCatching { downloadManager.getUriForDownloadedFile(downloadId) }.getOrNull()
                )
                DownloadManager.STATUS_FAILED -> DownloadStatus.Failed
                DownloadManager.STATUS_RUNNING,
                DownloadManager.STATUS_PENDING,
                DownloadManager.STATUS_PAUSED -> DownloadStatus.InProgress(progress)
                else -> DownloadStatus.Idle
            }
        }
        return DownloadStatus.Idle
    }
}
