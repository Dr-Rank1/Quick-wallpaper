package com.rank.quickwallpaper.ui.components

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener

@Composable
fun BannerAdSlot(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.height(50.dp),
            factory = { ctx ->
                Banner(ctx, object : BannerListener {
                    override fun onReceiveAd(view: View) {}
                    override fun onFailedToReceiveAd(view: View) {}
                    override fun onImpression(view: View) {}
                    override fun onClick(view: View) {}
                })
            }
        )
    }
}
