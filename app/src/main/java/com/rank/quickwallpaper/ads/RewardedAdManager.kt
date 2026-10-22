package com.rank.quickwallpaper.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RewardedAdManager"

@Singleton
class RewardedAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val unityAdsManager: UnityAdsManager
) {
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()
    private val isLoading = AtomicBoolean(false)

    fun load() {
        if (_ready.value || isLoading.get()) return
        isLoading.set(true)
        UnityAds.load(
            UnityAdsManager.REWARDED_PLACEMENT,
            object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String?) {
                    Log.d(TAG, "Unity Rewarded ad loaded: $placementId")
                    _ready.value = true
                    isLoading.set(false)
                }

                override fun onUnityAdsFailedToLoad(
                    placementId: String?,
                    error: UnityAds.UnityAdsLoadError?,
                    message: String?
                ) {
                    Log.e(TAG, "Unity Rewarded ad failed to load: $placementId, $message")
                    _ready.value = false
                    isLoading.set(false)
                }
            }
        )
    }

    fun show(activity: Activity, onRewarded: () -> Unit, onDismissed: () -> Unit) {
        if (!_ready.value) {
            load()
            onDismissed()
            return
        }

        _ready.value = false

        UnityAds.show(
            activity,
            UnityAdsManager.REWARDED_PLACEMENT,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String?,
                    error: UnityAds.UnityAdsShowError?,
                    message: String?
                ) {
                    Log.e(TAG, "Unity Rewarded ad show failure: $message")
                    load()
                    onDismissed()
                }

                override fun onUnityAdsShowStart(placementId: String?) {}

                override fun onUnityAdsShowClick(placementId: String?) {}

                override fun onUnityAdsShowComplete(
                    placementId: String?,
                    state: UnityAds.UnityAdsShowCompletionState?
                ) {
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        onRewarded()
                    }
                    load()
                    onDismissed()
                }
            }
        )
    }

    fun isReady() = _ready.value
}
