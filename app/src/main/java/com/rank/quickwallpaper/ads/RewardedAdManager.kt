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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class RewardedAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rewardedAd: StartAppAd? = null
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    fun load() {
        if (rewardedAd != null) return
        val loaded = StartAppAd(context)
        loaded.loadAd(AdMode.REWARDED_VIDEO, object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                rewardedAd = loaded
                _ready.value = true
            }
            override fun onFailedToReceiveAd(ad: Ad?) {
                rewardedAd = null
                _ready.value = false
            }
        })
    }

    fun show(activity: Activity, onRewarded: () -> Unit, onDismissed: () -> Unit) {
        val current = rewardedAd ?: run { onDismissed(); return }
        rewardedAd = null
        _ready.value = false
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

    fun isReady() = _ready.value
}
