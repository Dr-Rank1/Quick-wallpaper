package com.rank.quickwallpaper.service

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppReviewManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_APPLY_COUNT = intPreferencesKey("wallpaper_apply_count")
        private val KEY_RATED_OR_DISMISSED = booleanPreferencesKey("has_rated_or_dismissed")
        private const val TARGET_APPLY_TRIGGER = 3
    }

    suspend fun recordWallpaperApplied(): Boolean {
        val prefs = dataStore.data.first()
        val hasRated = prefs[KEY_RATED_OR_DISMISSED] ?: false
        if (hasRated) return false

        val currentCount = (prefs[KEY_APPLY_COUNT] ?: 0) + 1
        dataStore.edit { it[KEY_APPLY_COUNT] = currentCount }

        return currentCount == TARGET_APPLY_TRIGGER
    }

    suspend fun openPlayStore(activityContext: Context) {
        dataStore.edit { it[KEY_RATED_OR_DISMISSED] = true }
        runCatching {
            activityContext.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "market://details?id=${activityContext.packageName}".toUri()
                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            )
        }.onFailure {
            activityContext.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=${activityContext.packageName}".toUri()
                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            )
        }
    }

    suspend fun dismissForNow() {
        dataStore.edit {
            // Push count back to 1 so it asks again later
            it[KEY_APPLY_COUNT] = 1
        }
    }
}
