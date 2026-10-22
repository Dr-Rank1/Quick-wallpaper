package com.rank.quickwallpaper.ads

import android.app.Activity
import android.content.Context
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

// High-monetization frequency cap: 45 seconds between interstitials
private const val FREQUENCY_CAP_MS = 45 * 1000L

@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile private var interstitialAd: StartAppAd? = null
    @Volatile private var lastShownTime = 0L
    @Volatile private var isLoading = false
    private val actionCounter = AtomicInteger(0)

    init {
        load()
    }

    fun onForegrounded() {
        // Preload an ad whenever user comes back to the app
        if (interstitialAd == null) {
            load()
        }
    }

    fun load() {
        if (interstitialAd != null || isLoading) return
        isLoading = true
        val loaded = StartAppAd(context)
        loaded.loadAd(object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                interstitialAd = loaded
                isLoading = false
            }
            override fun onFailedToReceiveAd(ad: Ad?) {
                interstitialAd = null
                isLoading = false
            }
        })
    }

    /**
     * Checks if enough time has passed (45s) and shows the interstitial.
     * High monetization: does NOT permanently disable ads per session.
     */
    fun maybeShow(activity: Activity, force: Boolean = false, onDone: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        val elapsed = now - lastShownTime
        val canShow = force || (elapsed >= FREQUENCY_CAP_MS)

        val current = interstitialAd
        if (!canShow || current == null) {
            load()
            onDone()
            return
        }

        interstitialAd = null
        current.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad) {
                lastShownTime = System.currentTimeMillis()
                load()
                onDone()
            }
            override fun adDisplayed(ad: Ad) {
                lastShownTime = System.currentTimeMillis()
            }
            override fun adClicked(ad: Ad) {}
            override fun adNotDisplayed(ad: Ad) {
                load()
                onDone()
            }
        })
    }

    /**
     * Call on user actions (e.g. photo tapped, downloaded, set).
     * Automatically triggers an interstitial every 2-3 actions if cooldown has passed.
     */
    fun recordActionAndMaybeShow(activity: Activity, onDone: () -> Unit = {}) {
        val count = actionCounter.incrementAndGet()
        if (count % 2 == 0) {
            maybeShow(activity, force = false, onDone = onDone)
        } else {
            onDone()
        }
    }
}

