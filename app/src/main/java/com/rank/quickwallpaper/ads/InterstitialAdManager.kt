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

@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interstitialAd: StartAppAd? = null
    private var lastShownTime = 0L
    private var isFirstTime = true

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
        val canShow = !isFirstTime &&
            interstitialAd != null &&
            (now - lastShownTime) >= FREQUENCY_CAP_MS

        if (!canShow) {
            isFirstTime = false
            onDone()
            return
        }

        isFirstTime = false
        val current = interstitialAd ?: run { onDone(); return }
        current.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad) {
                interstitialAd = null
                lastShownTime = System.currentTimeMillis()
                load()
                onDone()
            }
            override fun adDisplayed(ad: Ad) {}
            override fun adClicked(ad: Ad) {}
            override fun adNotDisplayed(ad: Ad) { onDone() }
        })
    }
}
