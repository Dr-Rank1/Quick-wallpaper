package com.rank.quickwallpaper.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rank.quickwallpaper.R

enum class AppSkinId {
    MIDNIGHT,
    EDITORIAL,
    NEON,
    PAPER,
    MINIMAL;

    companion object {
        fun fromStorage(value: String?): AppSkinId =
            entries.firstOrNull { it.name == value } ?: MIDNIGHT
    }
}

/**
 * Full chrome look-pack: colors, type, and shapes.
 * Photo grids intentionally ignore these tokens.
 */
data class PaziaSkin(
    val id: AppSkinId,
    val displayName: String,
    val previewAccent: Color,
    val previewBackground: Color,
    val previewOnBackground: Color,
    val darkColorScheme: ColorScheme,
    val lightColorScheme: ColorScheme,
    val typography: Typography,
    val shapes: Shapes
)

private val InterFamily = FontFamily(Font(R.font.inter))
private val SerifFamily = FontFamily.Serif
private val SansFamily = FontFamily.SansSerif

private fun skinShapes(radius: Dp): Shapes {
    val r = radius
    return Shapes(
        extraSmall = RoundedCornerShape(r * 0.35f),
        small = RoundedCornerShape(r * 0.6f),
        medium = RoundedCornerShape(r),
        large = RoundedCornerShape(r * 1.35f),
        extraLarge = RoundedCornerShape(r * 1.7f)
    )
}

private fun buildTypography(
    displayFamily: FontFamily,
    bodyFamily: FontFamily,
    sizeScale: Float = 1f,
    letterSpacingExtra: Float = 0f,
    displayWeight: FontWeight = FontWeight.Bold,
    tightDisplayTracking: Boolean = false
): Typography {
    fun sp(v: Float) = (v * sizeScale).sp
    val displayTracking =
        if (tightDisplayTracking) (-1.0f + letterSpacingExtra).sp
        else ((-0.5f) + letterSpacingExtra).sp
    return Typography(
        displayLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = displayWeight,
            fontSize = sp(32f),
            lineHeight = sp(40f),
            letterSpacing = displayTracking
        ),
        displayMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = displayWeight,
            fontSize = sp(28f),
            lineHeight = sp(36f),
            letterSpacing = (if (tightDisplayTracking) -0.6f else -0.25f + letterSpacingExtra).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = displayWeight,
            fontSize = sp(24f),
            lineHeight = sp(32f),
            letterSpacing = letterSpacingExtra.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(20f),
            lineHeight = sp(28f),
            letterSpacing = letterSpacingExtra.sp
        ),
        titleLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = sp(18f),
            lineHeight = sp(24f),
            letterSpacing = letterSpacingExtra.sp
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = sp(16f),
            lineHeight = sp(22f),
            letterSpacing = (0.1f + letterSpacingExtra).sp
        ),
        titleSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = sp(14f),
            lineHeight = sp(20f),
            letterSpacing = (0.1f + letterSpacingExtra).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = sp(16f),
            lineHeight = sp(24f),
            letterSpacing = (0.3f + letterSpacingExtra).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = sp(14f),
            lineHeight = sp(20f),
            letterSpacing = (0.2f + letterSpacingExtra).sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = sp(12f),
            lineHeight = sp(16f),
            letterSpacing = (0.3f + letterSpacingExtra).sp
        ),
        labelLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = sp(13f),
            lineHeight = sp(18f),
            letterSpacing = (0.4f + letterSpacingExtra).sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = sp(11f),
            lineHeight = sp(16f),
            letterSpacing = (0.4f + letterSpacingExtra).sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = sp(10f),
            lineHeight = sp(14f),
            letterSpacing = (0.4f + letterSpacingExtra).sp
        )
    )
}

private fun darkScheme(
    primary: Color,
    onPrimary: Color = Color.White,
    secondary: Color,
    background: Color,
    surface: Color,
    surfaceVariant: Color,
    onBackground: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    outline: Color,
    error: Color = Color(0xFFE5484D)
) = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primary.copy(alpha = 0.16f),
    secondary = secondary,
    onSecondary = onPrimary,
    secondaryContainer = secondary.copy(alpha = 0.22f),
    background = background,
    surface = surface,
    surfaceVariant = surfaceVariant,
    surfaceTint = primary,
    onBackground = onBackground,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outline.copy(alpha = 0.5f),
    inverseSurface = onBackground,
    inverseOnSurface = background,
    error = error,
    errorContainer = error.copy(alpha = 0.2f),
    inversePrimary = primary.copy(alpha = 0.85f)
)

private fun lightScheme(
    primary: Color,
    onPrimary: Color = Color.White,
    secondary: Color,
    background: Color,
    surface: Color,
    surfaceVariant: Color,
    onBackground: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    outline: Color,
    error: Color = Color(0xFFB3261E)
) = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primary.copy(alpha = 0.12f),
    secondary = secondary,
    onSecondary = onPrimary,
    secondaryContainer = secondary.copy(alpha = 0.18f),
    background = background,
    surface = surface,
    surfaceVariant = surfaceVariant,
    surfaceTint = primary,
    onBackground = onBackground,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outline.copy(alpha = 0.5f),
    inverseSurface = onBackground,
    inverseOnSurface = background,
    error = error,
    errorContainer = error.copy(alpha = 0.12f),
    inversePrimary = primary
)

val MidnightSkin = PaziaSkin(
    id = AppSkinId.MIDNIGHT,
    displayName = "Midnight",
    previewAccent = Color(0xFF6C63FF),
    previewBackground = Color(0xFF000000),
    previewOnBackground = Color(0xFFEEEEF0),
    darkColorScheme = darkScheme(
        primary = Color(0xFF6C63FF),
        secondary = Color(0xFF8A8D98),
        background = Color(0xFF000000),
        surface = Color(0xFF0B0E14),
        surfaceVariant = Color(0xFF11131A),
        onBackground = Color(0xFFEEEEF0),
        onSurface = Color(0xFFEEEEF0),
        onSurfaceVariant = Color(0xFF8A8D98),
        outline = Color(0x1AFFFFFF)
    ),
    lightColorScheme = lightScheme(
        primary = Color(0xFF4A42D4),
        secondary = Color(0xFF5C5F6B),
        background = Color(0xFFF5F6F8),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFE8EAF0),
        onBackground = Color(0xFF12141A),
        onSurface = Color(0xFF12141A),
        onSurfaceVariant = Color(0xFF4E515A),
        outline = Color(0x1A12141A)
    ),
    typography = buildTypography(InterFamily, InterFamily),
    shapes = skinShapes(14.dp)
)

val EditorialSkin = PaziaSkin(
    id = AppSkinId.EDITORIAL,
    displayName = "Editorial",
    previewAccent = Color(0xFF1A1A1A),
    previewBackground = Color(0xFFF4EFE6),
    previewOnBackground = Color(0xFF1A1A1A),
    darkColorScheme = darkScheme(
        primary = Color(0xFFE8DCC8),
        onPrimary = Color(0xFF1A1A1A),
        secondary = Color(0xFFB8A99A),
        background = Color(0xFF141210),
        surface = Color(0xFF1C1916),
        surfaceVariant = Color(0xFF26211C),
        onBackground = Color(0xFFF4EFE6),
        onSurface = Color(0xFFF4EFE6),
        onSurfaceVariant = Color(0xFFB8A99A),
        outline = Color(0x33F4EFE6)
    ),
    lightColorScheme = lightScheme(
        primary = Color(0xFF1A1A1A),
        onPrimary = Color(0xFFF4EFE6),
        secondary = Color(0xFF6B5E52),
        background = Color(0xFFF4EFE6),
        surface = Color(0xFFFAF7F2),
        surfaceVariant = Color(0xFFE8E0D4),
        onBackground = Color(0xFF1A1A1A),
        onSurface = Color(0xFF1A1A1A),
        onSurfaceVariant = Color(0xFF5C534A),
        outline = Color(0x331A1A1A)
    ),
    typography = buildTypography(
        displayFamily = SerifFamily,
        bodyFamily = SerifFamily,
        sizeScale = 1.02f
    ),
    shapes = skinShapes(4.dp)
)

val NeonSkin = PaziaSkin(
    id = AppSkinId.NEON,
    displayName = "Neon",
    previewAccent = Color(0xFF00F0FF),
    previewBackground = Color(0xFF000000),
    previewOnBackground = Color(0xFFE8FFFF),
    darkColorScheme = darkScheme(
        primary = Color(0xFF00F0FF),
        onPrimary = Color(0xFF001418),
        secondary = Color(0xFFFF2BD6),
        background = Color(0xFF000000),
        surface = Color(0xFF05080C),
        surfaceVariant = Color(0xFF0A1218),
        onBackground = Color(0xFFE8FFFF),
        onSurface = Color(0xFFE8FFFF),
        onSurfaceVariant = Color(0xFF7AD4DE),
        outline = Color(0x3300F0FF)
    ),
    lightColorScheme = lightScheme(
        primary = Color(0xFF0097A7),
        onPrimary = Color.White,
        secondary = Color(0xFFC2185B),
        background = Color(0xFFF0FBFC),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFD4F4F7),
        onBackground = Color(0xFF042026),
        onSurface = Color(0xFF042026),
        onSurfaceVariant = Color(0xFF3A636A),
        outline = Color(0x330097A7)
    ),
    typography = buildTypography(
        displayFamily = SansFamily,
        bodyFamily = SansFamily,
        sizeScale = 1.06f,
        letterSpacingExtra = -0.35f,
        displayWeight = FontWeight.Black,
        tightDisplayTracking = true
    ),
    shapes = skinShapes(2.dp)
)

val PaperSkin = PaziaSkin(
    id = AppSkinId.PAPER,
    displayName = "Paper",
    previewAccent = Color(0xFFC45C26),
    previewBackground = Color(0xFFF7F1E8),
    previewOnBackground = Color(0xFF2B2A28),
    darkColorScheme = darkScheme(
        primary = Color(0xFFE08A5A),
        onPrimary = Color(0xFF2B160C),
        secondary = Color(0xFFC4B8A8),
        background = Color(0xFF1A1814),
        surface = Color(0xFF24211C),
        surfaceVariant = Color(0xFF2E2A24),
        onBackground = Color(0xFFF7F1E8),
        onSurface = Color(0xFFF7F1E8),
        onSurfaceVariant = Color(0xFFC4B8A8),
        outline = Color(0x33F7F1E8)
    ),
    lightColorScheme = lightScheme(
        primary = Color(0xFFC45C26),
        onPrimary = Color.White,
        secondary = Color(0xFF6E675C),
        background = Color(0xFFF7F1E8),
        surface = Color(0xFFFFFBF5),
        surfaceVariant = Color(0xFFEBE3D6),
        onBackground = Color(0xFF2B2A28),
        onSurface = Color(0xFF2B2A28),
        onSurfaceVariant = Color(0xFF6E675C),
        outline = Color(0x332B2A28)
    ),
    typography = buildTypography(
        displayFamily = SansFamily,
        bodyFamily = SansFamily,
        sizeScale = 1.04f,
        displayWeight = FontWeight.SemiBold
    ),
    shapes = skinShapes(24.dp)
)

val MinimalSkin = PaziaSkin(
    id = AppSkinId.MINIMAL,
    displayName = "Minimal",
    previewAccent = Color(0xFF888888),
    previewBackground = Color(0xFF111111),
    previewOnBackground = Color(0xFFEAEAEA),
    darkColorScheme = darkScheme(
        primary = Color(0xFFB0B0B0),
        onPrimary = Color(0xFF111111),
        secondary = Color(0xFF888888),
        background = Color(0xFF111111),
        surface = Color(0xFF181818),
        surfaceVariant = Color(0xFF222222),
        onBackground = Color(0xFFEAEAEA),
        onSurface = Color(0xFFEAEAEA),
        onSurfaceVariant = Color(0xFF9A9A9A),
        outline = Color(0x33FFFFFF)
    ),
    lightColorScheme = lightScheme(
        primary = Color(0xFF444444),
        onPrimary = Color.White,
        secondary = Color(0xFF777777),
        background = Color(0xFFF7F7F7),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFECECEC),
        onBackground = Color(0xFF1A1A1A),
        onSurface = Color(0xFF1A1A1A),
        onSurfaceVariant = Color(0xFF666666),
        outline = Color(0x331A1A1A)
    ),
    typography = buildTypography(
        displayFamily = InterFamily,
        bodyFamily = InterFamily,
        sizeScale = 0.96f,
        letterSpacingExtra = 1.2f,
        displayWeight = FontWeight.Medium
    ),
    shapes = skinShapes(8.dp)
)

val AllPaziaSkins: List<PaziaSkin> = listOf(
    MidnightSkin,
    EditorialSkin,
    NeonSkin,
    PaperSkin,
    MinimalSkin
)

fun AppSkinId.toSkin(): PaziaSkin =
    AllPaziaSkins.firstOrNull { it.id == this } ?: MidnightSkin
