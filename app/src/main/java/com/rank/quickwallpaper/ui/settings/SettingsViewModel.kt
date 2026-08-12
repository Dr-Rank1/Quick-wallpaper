package com.rank.quickwallpaper.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rank.quickwallpaper.ads.RewardedAdManager
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.service.WallpaperFit
import com.rank.quickwallpaper.worker.AutoWallpaperScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import android.app.Activity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DownloadQuality { MEDIUM, LARGE, FULL }

data class SettingsState(
    val useDynamicColors: Boolean = true,
    val useDarkTheme: Boolean = true,
    val downloadQuality: DownloadQuality = DownloadQuality.FULL,
    val wallpaperFit: WallpaperFit = WallpaperFit.FILL,
    val autoChangeEnabled: Boolean = false,
    val autoChangeHours: Int = 24,
    val adsRemoved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val autoWallpaperScheduler: AutoWallpaperScheduler,
    private val photoRepository: PhotoRepository,
    private val rewardedAdManager: RewardedAdManager
) : ViewModel() {

    companion object {
        val KEY_DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val KEY_DOWNLOAD_QUALITY = stringPreferencesKey("download_quality")
        val KEY_WALLPAPER_FIT = stringPreferencesKey("wallpaper_fit")
        val KEY_AUTO_CHANGE = booleanPreferencesKey("auto_change_enabled")
        val KEY_AUTO_CHANGE_HOURS = intPreferencesKey("auto_change_hours")
        val KEY_ADS_REMOVED = booleanPreferencesKey("ads_removed")
    }

    val settings: StateFlow<SettingsState> = dataStore.data.map { prefs ->
        SettingsState(
            useDynamicColors = prefs[KEY_DYNAMIC_COLORS] ?: true,
            useDarkTheme = prefs[KEY_DARK_THEME] ?: true,
            downloadQuality = prefs[KEY_DOWNLOAD_QUALITY]
                ?.let { runCatching { DownloadQuality.valueOf(it) }.getOrDefault(DownloadQuality.FULL) }
                ?: DownloadQuality.FULL,
            wallpaperFit = prefs[KEY_WALLPAPER_FIT]
                ?.let { runCatching { WallpaperFit.valueOf(it) }.getOrDefault(WallpaperFit.FILL) }
                ?: WallpaperFit.FILL,
            autoChangeEnabled = prefs[KEY_AUTO_CHANGE] ?: false,
            autoChangeHours = prefs[KEY_AUTO_CHANGE_HOURS] ?: 24,
            adsRemoved = prefs[KEY_ADS_REMOVED] ?: false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    init {
        viewModelScope.launch {
            val s = settings.value
            autoWallpaperScheduler.schedule(s.autoChangeEnabled, s.autoChangeHours.toLong())
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[KEY_DYNAMIC_COLORS] = enabled } }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[KEY_DARK_THEME] = enabled } }
    }

    fun setDownloadQuality(quality: DownloadQuality) {
        viewModelScope.launch { dataStore.edit { it[KEY_DOWNLOAD_QUALITY] = quality.name } }
    }

    fun setWallpaperFit(fit: WallpaperFit) {
        viewModelScope.launch { dataStore.edit { it[KEY_WALLPAPER_FIT] = fit.name } }
    }

    fun setAutoChange(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_AUTO_CHANGE] = enabled }
            val hours = settings.value.autoChangeHours
            autoWallpaperScheduler.schedule(enabled, hours.toLong())
        }
    }

    fun setAutoChangeHours(hours: Int) {
        viewModelScope.launch {
            val h = hours.coerceIn(1, 72)
            dataStore.edit { it[KEY_AUTO_CHANGE_HOURS] = h }
            autoWallpaperScheduler.schedule(settings.value.autoChangeEnabled, h.toLong())
        }
    }

    fun setAdsRemoved(removed: Boolean) {
        viewModelScope.launch { dataStore.edit { it[KEY_ADS_REMOVED] = removed } }
    }

    fun unlockAdsByRewarded(activity: Activity, onResult: (Boolean) -> Unit) {
        rewardedAdManager.load()
        rewardedAdManager.show(
            activity = activity,
            onRewarded = {
                viewModelScope.launch {
                    dataStore.edit { it[KEY_ADS_REMOVED] = true }
                    onResult(true)
                }
            },
            onDismissed = { onResult(false) }
        )
    }

    fun clearHistory() {
        viewModelScope.launch { photoRepository.clearHistory() }
    }

    fun markOnboardingDone() {
        viewModelScope.launch {
            dataStore.edit {
                it[KEY_ONBOARDING_DONE] = true
                it[KEY_FIRST_LAUNCH] = false
            }
        }
    }
}
