package com.rank.quickwallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.rank.quickwallpaper.ads.AppOpenAdManager
import com.rank.quickwallpaper.ads.InterstitialAdManager
import com.rank.quickwallpaper.ui.PaziaNavGraph
import com.rank.quickwallpaper.ui.onboarding.OnboardingScreen
import com.rank.quickwallpaper.ui.settings.SettingsViewModel
import com.rank.quickwallpaper.ui.theme.PaziaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appOpenAdManager: AppOpenAdManager
    @Inject lateinit var interstitialAdManager: InterstitialAdManager
    @Inject lateinit var dataStore: DataStore<Preferences>

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var isReady by mutableStateOf(false)
        var onboardingDone by mutableStateOf(false)

        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            val prefs = dataStore.data.first()
            val done = prefs[SettingsViewModel.KEY_ONBOARDING_DONE] ?: false
            val isFirst = prefs[SettingsViewModel.KEY_FIRST_LAUNCH] ?: true

            onboardingDone = done
            interstitialAdManager.load()
            appOpenAdManager.fetchAd()
            isReady = true

            if (!isFirst) {
                appOpenAdManager.tryShowAd(this@MainActivity) {}
            }
        }

        setContent {
            if (!isReady) return@setContent

            val settingsVm: SettingsViewModel = hiltViewModel()
            val settings by settingsVm.settings.collectAsStateWithLifecycle()

            PaziaTheme(
                darkTheme = settings.useDarkTheme,
                dynamicColor = settings.useDynamicColors
            ) {
                if (!onboardingDone) {
                    OnboardingScreen(
                        onFinish = {
                            settingsVm.markOnboardingDone()
                            onboardingDone = true
                        }
                    )
                } else {
                    PaziaNavGraph()
                }
            }
        }
    }

}
