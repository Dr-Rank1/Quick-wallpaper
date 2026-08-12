package com.rank.quickwallpaper.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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

private val LightColorScheme = lightColorScheme(
    primary = PaziaAccentDark,
    onPrimary = PaziaOnAccent,
    primaryContainer = PaziaAccent.copy(alpha = 0.12f),
    secondary = PaziaSecondaryDim,
    onSecondary = PaziaOnAccent,
    secondaryContainer = PaziaSecondaryLight.copy(alpha = 0.35f),
    background = PaziaLightBackground,
    surface = PaziaLightSurface,
    surfaceVariant = PaziaLightSurfaceVariant,
    surfaceTint = PaziaAccent,
    onBackground = PaziaLightOnBackground,
    onSurface = PaziaLightOnBackground,
    onSurfaceVariant = PaziaGray,
    outline = PaziaLightBorder,
    outlineVariant = PaziaLightBorder.copy(alpha = 0.5f),
    inverseSurface = PaziaSurface,
    inverseOnSurface = PaziaWhite,
    error = PaziaError,
    errorContainer = PaziaError.copy(alpha = 0.12f),
    inversePrimary = PaziaAccentLight
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PaziaTypography,
        content = content
    )
}
