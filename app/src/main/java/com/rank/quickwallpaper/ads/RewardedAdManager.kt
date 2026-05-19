package com.rank.quickwallpaper.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rewardedAd: RewardedAd? = null

    fun load() {
        RewardedAd.load(
            context,
            AdIds.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { rewardedAd = null }
            }
        )
    }

    fun show(activity: Activity, onRewarded: () -> Unit, onDismissed: () -> Unit) {
        val ad = rewardedAd ?: run { onDismissed(); return }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                load()
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) { onDismissed() }
        }
        ad.show(activity) { onRewarded() }
    }

    fun isReady() = rewardedAd != null
}
