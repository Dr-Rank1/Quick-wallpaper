package com.rank.quickwallpaper.ui.components

import android.content.Context
import androidx.lifecycle.ViewModel
import com.startapp.sdk.ads.nativead.NativeAdDetails
import com.startapp.sdk.ads.nativead.NativeAdPreferences
import com.startapp.sdk.ads.nativead.StartAppNativeAd
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NativeAdViewModel : ViewModel() {

    private val _nativeAd = MutableStateFlow<NativeAdDetails?>(null)
    val nativeAd: StateFlow<NativeAdDetails?> = _nativeAd.asStateFlow()

    private var startAppNativeAd: StartAppNativeAd? = null

    fun loadAd(context: Context) {
        if (_nativeAd.value != null || startAppNativeAd != null) return

        val preferences = NativeAdPreferences().apply {
            setAutoBitmapDownload(true)
        }
        val nativeAd = StartAppNativeAd(context).apply {
            setPreferences(preferences)
        }
        startAppNativeAd = nativeAd
        nativeAd.loadAd(object : AdEventListener {
            override fun onReceiveAd(ad: Ad) {
                _nativeAd.value = nativeAd.nativeAds?.firstOrNull()
            }
            override fun onFailedToReceiveAd(ad: Ad?) {
                _nativeAd.value = null
                startAppNativeAd = null
            }
        })
    }

    fun releaseAd() {
        _nativeAd.value?.unregisterView()
        _nativeAd.value = null
        startAppNativeAd = null
    }

    override fun onCleared() {
        releaseAd()
    }
}
