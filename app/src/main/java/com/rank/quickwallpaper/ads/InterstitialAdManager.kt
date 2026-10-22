package com.rank.quickwallpaper.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

private const val FREQUENCY_CAP_MS = 45 * 1000L
private const val TAG = "InterstitialAdManager"

@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val unityAdsManager: UnityAdsManager
) {
    @Volatile private var isLoaded = false
    private val isLoading = AtomicBoolean(false)
    @Volatile private var lastShownTime = 0L
    private val actionCounter = AtomicInteger(0)

    fun load() {
        if (isLoaded || isLoading.get()) return
        isLoading.set(true)
        UnityAds.load(
            UnityAdsManager.INTERSTITIAL_PLACEMENT,
            object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String?) {
                    Log.d(TAG, "Unity Interstitial loaded: $placementId")
                    isLoaded = true
                    isLoading.set(false)
                }

                override fun onUnityAdsFailedToLoad(
                    placementId: String?,
                    error: UnityAds.UnityAdsLoadError?,
                    message: String?
                ) {
                    Log.e(TAG, "Unity Interstitial failed to load: $placementId, err=$error, msg=$message")
                    isLoaded = false
                    isLoading.set(false)
                }
            }
        )
    }

    fun maybeShow(activity: Activity, force: Boolean = false, onDone: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        val elapsed = now - lastShownTime
        val canShow = force || (elapsed >= FREQUENCY_CAP_MS)

        if (!canShow || !isLoaded) {
            load()
            onDone()
            return
        }

        isLoaded = false
        UnityAds.show(
            activity,
            UnityAdsManager.INTERSTITIAL_PLACEMENT,
            UnityAdsShowOptions(),
            object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(
                    placementId: String?,
                    error: UnityAds.UnityAdsShowError?,
                    message: String?
                ) {
                    Log.e(TAG, "Unity Interstitial show failure: $placementId, $message")
                    load()
                    onDone()
                }

                override fun onUnityAdsShowStart(placementId: String?) {
                    lastShownTime = System.currentTimeMillis()
                }

                override fun onUnityAdsShowClick(placementId: String?) {}

                override fun onUnityAdsShowComplete(
                    placementId: String?,
                    state: UnityAds.UnityAdsShowCompletionState?
                ) {
                    lastShownTime = System.currentTimeMillis()
                    load()
                    onDone()
                }
            }
        )
    }

    fun recordActionAndMaybeShow(activity: Activity, onDone: () -> Unit = {}) {
        val count = actionCounter.incrementAndGet()
        if (count % 2 == 0) {
            maybeShow(activity, force = false, onDone = onDone)
        } else {
            onDone()
        }
    }

    fun onForegrounded() {
        load()
    }
}
