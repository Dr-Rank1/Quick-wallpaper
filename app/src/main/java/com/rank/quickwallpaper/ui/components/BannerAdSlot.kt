package com.rank.quickwallpaper.ui.components

import android.app.Activity
import android.util.Log
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

    DisposableEffect(activity) {
        val banner = BannerView(
            activity,
            UnityAdsManager.BANNER_PLACEMENT,
            UnityBannerSize(320, 50)
        )
        banner.listener = object : BannerView.IListener {
            override fun onBannerLoaded(bannerView: BannerView?) {
                Log.d("BannerAdSlot", "Unity Banner loaded successfully")
            }

            override fun onBannerShown(bannerView: BannerView?) {
                Log.d("BannerAdSlot", "Unity Banner shown")
            }

            override fun onBannerFailedToLoad(bannerView: BannerView?, errorInfo: BannerErrorInfo?) {
                Log.d("BannerAdSlot", "Unity Banner failed to load: ${errorInfo?.errorMessage}")
            }

            override fun onBannerClick(bannerView: BannerView?) {}
            override fun onBannerLeftApplication(bannerView: BannerView?) {}
        }
        banner.load()
        bannerView = banner

        onDispose {
            banner.destroy()
            bannerView = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        bannerView?.let { bv ->
            AndroidView(
                factory = { bv },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            )
        }
    }
}
