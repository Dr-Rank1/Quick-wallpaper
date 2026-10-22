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

private const val RESUME_COOLDOWN_MS = 60 * 1000L

@Singleton
class AppOpenAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile private var appOpenAd: StartAppAd? = null
    @Volatile private var lastShownTime = 0L
    @Volatile private var isLoading = false

    init {
        load()
    }

    fun load() {
        if (appOpenAd != null || isLoading) return
        isLoading = true
        val loaded = StartAppAd(context)
        loaded.loadAd(object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                appOpenAd = loaded
                isLoading = false
            }
            override fun onFailedToReceiveAd(ad: Ad?) {
                appOpenAd = null
                isLoading = false
            }
        })
    }

    fun maybeShow(activity: Activity, adsRemoved: Boolean, onDone: () -> Unit = {}) {
        if (adsRemoved) {
            onDone()
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastShownTime < RESUME_COOLDOWN_MS) {
            onDone()
            return
        }

        val current = appOpenAd
        if (current == null) {
            load()
            onDone()
            return
        }
        appOpenAd = null
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

    fun onForegrounded(activity: Activity? = null, adsRemoved: Boolean = false) {
        if (activity != null) {
            maybeShow(activity, adsRemoved)
        } else {
            load()
        }
    }
}

