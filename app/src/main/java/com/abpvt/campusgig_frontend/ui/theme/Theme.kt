/**
 * CampusGig Material Theme — v2.0
 *
 * Implements the new 4-layer dark surface system with Electric Indigo + Violet palette.
 * Dynamic color (Android 12+ wallpaper-based) is intentionally DISABLED to preserve
 * CampusGig brand identity across all devices.
 *
 * Surface hierarchy:
 *   background  = Surface1   (#0F1117) — main screen bg
 *   surface     = Surface2   (#141820) — cards, lists
 *   surfaceVariant = Surface3 (#1A1F2E) — inputs, chips, elevated
 *   surfaceContainerHigh = Surface4  — active elevated state
 *
 * Primary action flow:
 *   primary  = Indigo400 (#818CF8) in dark / Indigo600 (#4F46E5) in light
 *   secondary = Violet500 (#8B5CF6)
 *   tertiary  = SemanticWarning (#F59E0B) — for ratings/badges
 */
package com.abpvt.campusgig_frontend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Dark Color Scheme ────────────────────────────────────────────────────────
private val CampusGigDarkColorScheme = darkColorScheme(
    primary             = Indigo400,
    onPrimary           = Color.White,
    primaryContainer    = Indigo700,
    onPrimaryContainer  = Violet400,

    secondary           = Violet500,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFF2D1B69),
    onSecondaryContainer = Violet400,

    tertiary            = SemanticWarning,
    onTertiary          = Color(0xFF1A1200),
    tertiaryContainer   = Color(0xFF3B2C00),
    onTertiaryContainer = GradientGoldEnd,

    background          = Surface1,
    onBackground        = TextPrimary,

    surface             = Surface2,
    onSurface           = TextPrimary,

    surfaceVariant      = Surface3,
    onSurfaceVariant    = TextSecondary,

    surfaceContainerHighest = Surface4,
    surfaceContainer    = Surface3,
    surfaceContainerHigh= Surface4,
    surfaceContainerLow = Surface2,
    surfaceContainerLowest = BackgroundBase,

    outline             = BorderSubtle,
    outlineVariant      = BorderDefault,

    error               = SemanticError,
    onError             = Color.White,
    errorContainer      = SemanticErrorBg,
    onErrorContainer    = SemanticError,

    inverseSurface      = TextPrimary,
    inverseOnSurface    = Surface1,
    inversePrimary      = Indigo600,

    scrim               = ScrimColor
)

// ─── Light Color Scheme ───────────────────────────────────────────────────────
private val CampusGigLightColorScheme = lightColorScheme(
    primary             = Indigo600,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFDDE1FF),
    onPrimaryContainer  = Indigo700,

    secondary           = Violet600,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFEDE9FE),
    onSecondaryContainer = Violet600,

    tertiary            = CampusAmberDark,
    onTertiary          = Color.White,
    tertiaryContainer   = Color(0xFFFFEFB0),
    onTertiaryContainer = Color(0xFF3B2C00),

    background          = BackgroundBaseLight,
    onBackground        = TextInverted,

    surface             = Surface1Light,
    onSurface           = TextInverted,

    surfaceVariant      = Surface3Light,
    onSurfaceVariant    = TextTertiary,

    outline             = BorderSubtleLight,
    outlineVariant      = BorderDefaultLight,

    error               = ErrorColorLight,
    onError             = Color.White
)

/**
 * CampusGigTheme — Root composable that wraps the entire app in Material3 theming.
 *
 * @param darkTheme  Whether to use dark mode. Defaults to system setting.
 * @param content    The composable content to theme.
 */
@Composable
fun CampusGigTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CampusGigDarkColorScheme else CampusGigLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}

// Backward-compat alias
@Composable
fun Campusgig_frontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = CampusGigTheme(darkTheme = darkTheme, content = content)