package com.rank.quickwallpaper.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PaziaAccent,
    onPrimary = PaziaOnAccent,
    primaryContainer = PaziaAccentVariant,
    background = PaziaBlack,
    surface = PaziaSurface,
    surfaceVariant = PaziaCard,
    onBackground = PaziaWhite,
    onSurface = PaziaWhite,
    onSurfaceVariant = PaziaGrayLight,
    error = PaziaError,
    outline = PaziaElevated
)

private val LightColorScheme = lightColorScheme(
    primary = PaziaAccentVariant,
    onPrimary = PaziaWhite,
    background = PaziaWhite,
    surface = PaziaWhite,
    onBackground = PaziaBlack,
    onSurface = PaziaBlack
)

@Composable
fun PaziaTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PaziaBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PaziaTypography,
        content = content
    )
}
