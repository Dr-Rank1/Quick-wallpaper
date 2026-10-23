package com.rank.quickwallpaper.ui.components

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.rank.quickwallpaper.ads.UnityAdsManager
import com.unity3d.ads.UnityAds
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

@Composable
fun BannerAdSlot(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    var bannerView by remember { mutableStateOf<BannerView?>(null) }
    var isAdLoaded by remember { mutableStateOf(false) }

    DisposableEffect(activity) {
        var banner: BannerView? = null
        var isDisposed = false
        val handler = Handler(Looper.getMainLooper())

        fun createAndLoadBanner() {
            if (isDisposed) return
            try {
                if (UnityAds.isInitialized) {
                    banner?.destroy()
                    banner = BannerView(
                        activity,
                        UnityAdsManager.BANNER_PLACEMENT,
                        UnityBannerSize(320, 50)
                    ).apply {
                        listener = object : BannerView.IListener {
                            override fun onBannerLoaded(bv: BannerView?) {
                                Log.d("BannerAdSlot", "Unity Banner loaded successfully")
                                if (!isDisposed) {
                                    isAdLoaded = true
                                    bannerView = bv
                                }
                            }

                            override fun onBannerShown(bv: BannerView?) {
                                Log.d("BannerAdSlot", "Unity Banner shown")
                            }

                            override fun onBannerFailedToLoad(bv: BannerView?, errorInfo: BannerErrorInfo?) {
                                Log.d("BannerAdSlot", "Unity Banner failed to load: ${errorInfo?.errorMessage}")
                                isAdLoaded = false
                            }

                            override fun onBannerClick(bv: BannerView?) {}
                            override fun onBannerLeftApplication(bv: BannerView?) {}
                        }
                        load()
                    }
                }
            } catch (e: Exception) {
                Log.e("BannerAdSlot", "Error creating/loading banner", e)
            }
        }

        // Try load immediately
        createAndLoadBanner()

        // Polling check/retry every 10s if not initialized or not loaded
        val retryRunnable = object : Runnable {
            override fun run() {
                if (isDisposed) return
                if (!isAdLoaded && UnityAds.isInitialized) {
                    createAndLoadBanner()
                }
                if (!isAdLoaded) {
                    handler.postDelayed(this, 12000L)
                }
            }
        }
        handler.postDelayed(retryRunnable, 2500L)

        onDispose {
            isDisposed = true
            handler.removeCallbacksAndMessages(null)
            try {
                banner?.destroy()
            } catch (e: Exception) {
                Log.e("BannerAdSlot", "Error destroying banner", e)
            }
            bannerView = null
            isAdLoaded = false
        }
    }

    AnimatedVisibility(
        visible = isAdLoaded && bannerView != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            bannerView?.let { bv ->
                AndroidView(
                    factory = { ctx ->
                        (bv.parent as? ViewGroup)?.removeView(bv)
                        bv
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )
            }
        }
    }
}
