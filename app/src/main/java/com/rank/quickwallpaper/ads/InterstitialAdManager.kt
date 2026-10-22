package com.rank.quickwallpaper.ads

import android.app.Activity
import android.content.Context
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val FREQUENCY_CAP_MS = 4 * 60 * 1000L
private const val MIN_FOREGROUND_MS = 30 * 1000L

@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile private var interstitialAd: StartAppAd? = null
    @Volatile private var lastShownTime = 0L
    @Volatile private var foregroundSinceMs = System.currentTimeMillis()
    @Volatile private var sessionShown = false

    fun onForegrounded() {
        foregroundSinceMs = System.currentTimeMillis()
        sessionShown = false
    }

    fun load() {
        if (interstitialAd != null) return
        val loaded = StartAppAd(context)
        loaded.loadAd(object : AdEventListener {
            override fun onReceiveAd(ad: Ad) { interstitialAd = loaded }
            override fun onFailedToReceiveAd(ad: Ad?) { interstitialAd = null }
        })
    }

    fun maybeShow(activity: Activity, onDone: () -> Unit) {
        val now = System.currentTimeMillis()
        val canShow = !sessionShown &&
            interstitialAd != null &&
            (now - foregroundSinceMs) >= MIN_FOREGROUND_MS &&
            (now - lastShownTime) >= FREQUENCY_CAP_MS

        if (!canShow) {
            onDone()
            return
        }

        val current = interstitialAd ?: run { onDone(); return }
        interstitialAd = null
        current.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad) {
                sessionShown = true
                lastShownTime = System.currentTimeMillis()
                load()
                onDone()
            }
            override fun adDisplayed(ad: Ad) {}
            override fun adClicked(ad: Ad) {}
            override fun adNotDisplayed(ad: Ad) {
                load()
                onDone()
            }
        })
    }
}
