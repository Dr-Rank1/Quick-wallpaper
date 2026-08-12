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

@Singleton
class AppOpenAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var appOpenAd: StartAppAd? = null
    private var shownThisSession = false

    fun load() {
        if (appOpenAd != null) return
        val loaded = StartAppAd(context)
        loaded.loadAd(object : AdEventListener {
            override fun onReceiveAd(ad: Ad) { appOpenAd = loaded }
            override fun onFailedToReceiveAd(ad: Ad?) { appOpenAd = null }
        })
    }

    fun maybeShow(activity: Activity, adsRemoved: Boolean, onDone: () -> Unit = {}) {
        if (adsRemoved || shownThisSession) {
            onDone()
            return
        }
        val current = appOpenAd
        if (current == null) {
            load()
            onDone()
            return
        }
        shownThisSession = true
        appOpenAd = null
        current.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad) {
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

    fun onForegrounded() {
        // Keep session flag; only cold path clears via new process. Soft reload.
        load()
    }
}
