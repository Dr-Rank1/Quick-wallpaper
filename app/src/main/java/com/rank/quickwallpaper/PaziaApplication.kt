package com.rank.quickwallpaper

import android.app.Application
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class PaziaApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var okHttpClient: OkHttpClient

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupCoil()
        MobileAds.initialize(this)
        // Add your hashed test device ID here (find it in logcat after the first ad load).
        // This ensures YOUR device never triggers real ad clicks, even on release builds.
        // Search logcat for "Use RequestConfiguration.Builder().setTestDeviceIds" to get the ID.
        val testDeviceIds = listOf<String>(
            // "YOUR_HASHED_DEVICE_ID_HERE"
        )
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        )
    }

    private fun setupCoil() {
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.30)   // 30% of available RAM
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(500L * 1024 * 1024)  // 500 MB disk cache
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .respectCacheHeaders(false)  // Pexels CDN headers are short — ignore them
            .apply {
                if (BuildConfig.DEBUG) logger(DebugLogger())
            }
            .build()

        Coil.setImageLoader(imageLoader)
    }
}
