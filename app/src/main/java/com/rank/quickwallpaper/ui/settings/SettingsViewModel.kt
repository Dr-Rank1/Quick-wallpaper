package com.rank.quickwallpaper.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rank.quickwallpaper.ui.theme.AppSkinId
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val selectedSkin: AppSkinId = AppSkinId.MIDNIGHT,
    val downloadQuality: DownloadQuality = DownloadQuality.FULL
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val KEY_DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val KEY_DOWNLOAD_QUALITY = stringPreferencesKey("download_quality")
        val KEY_SELECTED_APP_SKIN = stringPreferencesKey("selected_app_skin")
    }

    val settings: StateFlow<SettingsState> = dataStore.data.map { prefs ->
        SettingsState(
            useDynamicColors = prefs[KEY_DYNAMIC_COLORS] ?: true,
            useDarkTheme = prefs[KEY_DARK_THEME] ?: true,
            selectedSkin = AppSkinId.fromStorage(prefs[KEY_SELECTED_APP_SKIN]),
            downloadQuality = prefs[KEY_DOWNLOAD_QUALITY]
                ?.let { runCatching { DownloadQuality.valueOf(it) }.getOrDefault(DownloadQuality.FULL) }
                ?: DownloadQuality.FULL
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_DYNAMIC_COLORS] = enabled
                // Dynamic colors only apply cleanly on Midnight.
                if (enabled) prefs[KEY_SELECTED_APP_SKIN] = AppSkinId.MIDNIGHT.name
            }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { dataStore.edit { it[KEY_DARK_THEME] = enabled } }
    }

    fun setAppSkin(skinId: AppSkinId) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_SELECTED_APP_SKIN] = skinId.name
                // Custom skins own their palette — turn off Material You.
                if (skinId != AppSkinId.MIDNIGHT) {
                    prefs[KEY_DYNAMIC_COLORS] = false
                }
            }
        }
    }

    fun setDownloadQuality(quality: DownloadQuality) {
        viewModelScope.launch { dataStore.edit { it[KEY_DOWNLOAD_QUALITY] = quality.name } }
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
