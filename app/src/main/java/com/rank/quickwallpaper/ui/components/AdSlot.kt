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
import com.rank.quickwallpaper.R
import com.startapp.sdk.ads.nativead.NativeAdDetails
import java.util.Locale

@Composable
fun NativeAdSlot(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adVm: NativeAdViewModel = viewModel()
    var nativeAd by remember { mutableStateOf<NativeAdDetails?>(null) }

    DisposableEffect(Unit) {
        adVm.loadAd(context)
        onDispose { adVm.releaseAd() }
    }

    val ad = adVm.nativeAd
    if (ad == null) return

    AndroidView(
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        factory = { ctx -> inflateAndBind(ctx, ad) }
    )
}

@SuppressLint("InflateParams")
private fun inflateAndBind(context: Context, ad: NativeAdDetails): View {
    val view = LayoutInflater.from(context).inflate(R.layout.native_ad_grid_item, null)
    bindNativeAd(view, ad)
    return view
}

private fun bindNativeAd(view: View, ad: NativeAdDetails) {
    val headlineView = view.findViewById<TextView>(R.id.ad_headline)
    headlineView.text = ad.title

    val bodyView = view.findViewById<TextView>(R.id.ad_body)
    if (ad.description.isNotBlank()) {
        bodyView.text = ad.description
        bodyView.visibility = View.VISIBLE
    } else {
        bodyView.visibility = View.GONE
    }

    val mediaView = view.findViewById<ImageView>(R.id.ad_media)
    if (ad.imageBitmap != null) {
        mediaView.setImageBitmap(ad.imageBitmap)
        mediaView.visibility = View.VISIBLE
    } else {
        mediaView.visibility = View.GONE
    }

    val ctaView = view.findViewById<Button>(R.id.ad_call_to_action)
    if (ad.callToAction.isNotBlank()) {
        ctaView.text = ad.callToAction
        ctaView.visibility = View.VISIBLE
    } else {
        ctaView.visibility = View.GONE
    }

    val metaRow = view.findViewById<View>(R.id.ad_meta_row)
    val ratingView = view.findViewById<TextView>(R.id.ad_rating)
    val categoryView = view.findViewById<TextView>(R.id.ad_category)

    val ratingText = if (ad.rating > 0f) String.format(Locale.US, "★ %.1f", ad.rating) else null
    val categoryText = ad.category.takeIf { it.isNotBlank() }

    if (ratingText == null && categoryText == null) {
        metaRow.visibility = View.GONE
    } else {
        metaRow.visibility = View.VISIBLE
        ratingView.text = ratingText
        ratingView.visibility = if (ratingText != null) View.VISIBLE else View.GONE
        categoryView.text = if (categoryText != null && ratingText != null) " · $categoryText" else categoryText
        categoryView.visibility = if (categoryText != null) View.VISIBLE else View.GONE
    }

    ad.registerViewForInteraction(view)
}
