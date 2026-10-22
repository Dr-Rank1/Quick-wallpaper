package com.rank.quickwallpaper.ads

import android.app.Activity
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val interstitialAdManager: InterstitialAdManager
) {
    fun load() {
        interstitialAdManager.load()
    }

    fun maybeShow(activity: Activity, adsRemoved: Boolean, onDone: () -> Unit = {}) {
        if (adsRemoved) {
            onDone()
            return
        }
        interstitialAdManager.maybeShow(activity, force = false, onDone = onDone)
    }

    fun onForegrounded(activity: Activity? = null, adsRemoved: Boolean = false) {
        if (activity != null && !adsRemoved) {
            maybeShow(activity, adsRemoved)
        } else {
            load()
        }
    }
}
