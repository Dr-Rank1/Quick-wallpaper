package com.rank.quickwallpaper.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val FREQUENCY_CAP_MS = 4 * 60 * 1000L

@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interstitialAd: InterstitialAd? = null
    private var lastShownTime = 0L
    private var isFirstTime = true

    fun load() {
        InterstitialAd.load(
            context,
            AdIds.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
                override fun onAdFailedToLoad(error: LoadAdError) { interstitialAd = null }
            }
        )
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

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastShownTime = System.currentTimeMillis()
                load()
                onDone()
            }
            override fun onAdFailedToShowFullScreenContent(e: com.google.android.gms.ads.AdError) {
                onDone()
            }
        }
        interstitialAd?.show(activity) ?: onDone()
    }
}
