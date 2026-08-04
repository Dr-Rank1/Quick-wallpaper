package com.rank.quickwallpaper.ui.components

import android.content.Context
import androidx.lifecycle.ViewModel
import com.startapp.sdk.ads.nativead.NativeAdDetails
import com.startapp.sdk.ads.nativead.NativeAdPreferences
import com.startapp.sdk.ads.nativead.StartAppNativeAd
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.adlisteners.AdEventListener

class NativeAdViewModel : ViewModel() {

    private var _nativeAd: NativeAdDetails? = null
    val nativeAd: NativeAdDetails? get() = _nativeAd

    private var startAppNativeAd: StartAppNativeAd? = null

    fun loadAd(context: Context) {
        if (_nativeAd != null || startAppNativeAd != null) return

        val preferences = NativeAdPreferences().apply {
            setAutoBitmapDownload(true)
        }
        val nativeAd = StartAppNativeAd(context).apply {
            setPreferences(preferences)
        }
        startAppNativeAd = nativeAd
        nativeAd.loadAd(object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                _nativeAd = nativeAd.nativeAds?.firstOrNull()
            }
            override fun onFailedToReceiveAd(ad: Ad?) {
                _nativeAd = null
                startAppNativeAd = null
            }
        })
    }

    fun releaseAd() {
        _nativeAd?.unregisterView()
        _nativeAd = null
        startAppNativeAd = null
    }

    override fun onCleared() {
        releaseAd()
    }
}
