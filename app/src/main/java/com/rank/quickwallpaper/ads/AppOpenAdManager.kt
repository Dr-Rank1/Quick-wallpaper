package com.rank.quickwallpaper.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var appOpenAd: AppOpenAd? = null
    private var loadTime: Long = 0
    private var isShowingAd = false

    private fun isAdAvailable() =
        appOpenAd != null && (Date().time - loadTime) < 4 * 60 * 60 * 1000

    fun fetchAd() {
        if (isAdAvailable()) return
        AppOpenAd.load(
            context,
            AdIds.APP_OPEN,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTime = Date().time
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                }
            }
        )
    }

    fun tryShowAd(activity: Activity, onComplete: () -> Unit) {
        if (isShowingAd || !isAdAvailable()) {
            onComplete()
            return
        }
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                onComplete()
                fetchAd()
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                isShowingAd = false
                onComplete()
            }
            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
            }
        }
        appOpenAd?.show(activity) ?: onComplete()
    }
}
