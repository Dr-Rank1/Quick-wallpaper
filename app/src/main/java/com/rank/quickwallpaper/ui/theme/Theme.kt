package com.rank.quickwallpaper.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalPaziaSkin = staticCompositionLocalOf { MidnightSkin }

@Composable
fun PaziaTheme(
    skin: PaziaSkin = MidnightSkin,
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Dynamic Material You only for Midnight so custom skins keep their identity.
    val useDynamic = dynamicColor &&
        skin.id == AppSkinId.MIDNIGHT &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when {
        useDynamic -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> skin.darkColorScheme
        else -> skin.lightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalPaziaSkin provides skin) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = skin.typography,
            shapes = skin.shapes,
            content = content
        )
    }
}
