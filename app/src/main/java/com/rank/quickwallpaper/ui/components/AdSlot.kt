package com.rank.quickwallpaper.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.rank.quickwallpaper.R
import com.rank.quickwallpaper.ads.AdIds

@Composable
fun NativeAdSlot(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adVm: NativeAdViewModel = viewModel()
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        adVm.loadAd(context)
        onDispose { adVm.releaseAd() }
    }

    val ad = adVm.nativeAd
    if (ad == null) return

    AndroidView(
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        factory = { ctx -> inflateAndBind(ctx, ad) },
        update = { view -> bindNativeAd(view as? NativeAdView ?: return@AndroidView, ad) }
    )
}

@SuppressLint("InflateParams")
private fun inflateAndBind(context: Context, ad: NativeAd): NativeAdView {
    val view = LayoutInflater.from(context)
        .inflate(R.layout.native_ad_grid_item, null) as NativeAdView
    bindNativeAd(view, ad)
    return view
}

private fun bindNativeAd(adView: NativeAdView, ad: NativeAd) {
    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
    headlineView.text = ad.headline
    adView.headlineView = headlineView

    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
    if (ad.body != null) {
        bodyView.text = ad.body
        bodyView.visibility = View.VISIBLE
    } else {
        bodyView.visibility = View.GONE
    }
    adView.bodyView = bodyView

    val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
    if (ad.icon != null) {
        iconView.setImageDrawable(ad.icon!!.drawable)
        iconView.visibility = View.VISIBLE
    } else {
        iconView.visibility = View.GONE
    }
    adView.iconView = iconView

    val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
    adView.mediaView = mediaView

    val ctaView = adView.findViewById<Button>(R.id.ad_call_to_action)
    if (ad.callToAction != null) {
        ctaView.text = ad.callToAction
        ctaView.visibility = View.VISIBLE
    } else {
        ctaView.visibility = View.GONE
    }
    adView.callToActionView = ctaView

    adView.setNativeAd(ad)
}
