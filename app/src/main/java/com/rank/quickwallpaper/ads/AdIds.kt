package com.rank.quickwallpaper.ads

import com.rank.quickwallpaper.BuildConfig

object AdIds {
    val BANNER: String = BuildConfig.ADMOB_BANNER_ID
    val INTERSTITIAL: String = BuildConfig.ADMOB_INTERSTITIAL_ID
    val NATIVE: String = BuildConfig.ADMOB_NATIVE_ID
    // TODO: Replace ADMOB_APP_OPEN_ID in build.gradle.kts with a real production ID from AdMob console
    val APP_OPEN: String = BuildConfig.ADMOB_APP_OPEN_ID
    // TODO: Replace ADMOB_REWARDED_ID in build.gradle.kts with a real production ID from AdMob console
    val REWARDED: String = BuildConfig.ADMOB_REWARDED_ID
}
