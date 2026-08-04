package com.rank.quickwallpaper.ads

import android.app.Activity
import android.content.Context
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppAd.AdMode
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rewardedAd: StartAppAd? = null

    fun load() {
        if (rewardedAd != null) return
        val loaded = StartAppAd(context)
        loaded.loadAd(AdMode.REWARDED_VIDEO, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) { rewardedAd = loaded }
            override fun onFailedToReceiveAd(ad: Ad?) { rewardedAd = null }
        })
    }

    fun show(activity: Activity, onRewarded: () -> Unit, onDismissed: () -> Unit) {
        val current = rewardedAd ?: run { onDismissed(); return }
        rewardedAd = null
        current.setVideoListener { onRewarded() }
        current.showAd(object : AdDisplayListener {
            override fun adHidden(ad: Ad) {
                load()
                onDismissed()
            }
            override fun adDisplayed(ad: Ad) {}
            override fun adClicked(ad: Ad) {}
            override fun adNotDisplayed(ad: Ad) { onDismissed() }
        })
    }

    fun isReady() = rewardedAd != null
}
