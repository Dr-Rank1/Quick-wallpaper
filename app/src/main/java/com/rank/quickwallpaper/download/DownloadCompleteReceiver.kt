package com.rank.quickwallpaper.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DownloadEventBus {
    private val _events = MutableSharedFlow<Long>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    fun emit(downloadId: Long) {
        _events.tryEmit(downloadId)
    }
}

class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE || context == null) return
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return

        // DownloadManager only returns this app's downloads, so spoofed IDs are ignored.
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val owned = dm.query(DownloadManager.Query().setFilterById(downloadId))
            ?.use { it.moveToFirst() } == true
        if (!owned) return

        DownloadEventBus.emit(downloadId)
    }
}
