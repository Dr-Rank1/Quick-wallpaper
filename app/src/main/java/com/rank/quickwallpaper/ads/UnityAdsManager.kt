package com.rank.quickwallpaper.ads

import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnityAdsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val GAME_ID = "6211137"
        const val INTERSTITIAL_PLACEMENT = "Interstitial_Android"
        const val REWARDED_PLACEMENT = "Rewarded_Android"
        const val BANNER_PLACEMENT = "Banner_Android"
        private const val TAG = "UnityAdsManager"
    }

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    fun initialize(testMode: Boolean = false) {
        if (UnityAds.isInitialized) {
            _isInitialized.value = true
            return
        }
        UnityAds.initialize(
            context,
            GAME_ID,
            testMode,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    Log.d(TAG, "Unity Ads initialized successfully (Game ID: $GAME_ID, testMode: $testMode)")
                    _isInitialized.value = true
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError?,
                    message: String?
                ) {
                    Log.e(TAG, "Unity Ads initialization failed: $error - $message")
                    _isInitialized.value = false
                }
            }
        )
    }
}
