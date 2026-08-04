package com.rank.quickwallpaper.ui.components

import androidx.lifecycle.ViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.rank.quickwallpaper.ads.AdIds

class NativeAdViewModel : ViewModel() {

    private var _nativeAd: NativeAd? = null
    val nativeAd: NativeAd? get() = _nativeAd

    private var loader: AdLoader? = null

    fun loadAd(context: android.content.Context) {
        if (_nativeAd != null) return
        if (loader == null) {
            loader = AdLoader.Builder(context, AdIds.NATIVE)
                .forNativeAd { ad -> _nativeAd = ad }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(e: LoadAdError) { _nativeAd = null }
                })
                .build()
        }
        loader?.loadAd(AdRequest.Builder().build())
    }

    fun releaseAd() {
        _nativeAd?.destroy()
        _nativeAd = null
    }

    override fun onCleared() {
        _nativeAd?.destroy()
        _nativeAd = null
    }
}
