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
        if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId != -1L) DownloadEventBus.emit(downloadId)
    }
}
