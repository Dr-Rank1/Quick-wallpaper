package com.rank.quickwallpaper.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PaziaAccent,
    onPrimary = PaziaOnAccent,
    primaryContainer = PaziaAccent.copy(alpha = 0.12f),
    secondary = PaziaSecondary,
    onSecondary = PaziaOnAccent,
    secondaryContainer = PaziaSecondaryDim,
    background = PaziaBlack,
    surface = PaziaSurface,
    surfaceVariant = PaziaSurfaceVariant,
    surfaceTint = PaziaAccent,
    onBackground = PaziaWhite,
    onSurface = PaziaWhite,
    onSurfaceVariant = PaziaGrayLight,
    outline = PaziaBorderMedium,
    outlineVariant = PaziaBorderLight,
    inverseSurface = PaziaWhiteDim,
    inverseOnSurface = PaziaBlack,
    error = PaziaError,
    errorContainer = PaziaErrorDim,
    inversePrimary = PaziaAccentLight
)

@Composable
fun PaziaTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        else -> DarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PaziaBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PaziaTypography,
        content = content
    )
}
