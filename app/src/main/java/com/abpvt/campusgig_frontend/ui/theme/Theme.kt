/**
 * CampusVault Material Theme — v3.0
 *
 * Implements the new 4-layer dark surface system with Electric Indigo + Violet palette,
 * AND a vibrant campus-energy light mode with Campus Blue as the primary action color.
 *
 * Dynamic color (Android 12+ wallpaper-based) is intentionally DISABLED to preserve
 * CampusVault brand identity across all devices.
 *
 * Dark mode surface hierarchy:
 *   background  = Surface1   (#0F1117) — main screen bg
 *   surface     = Surface2   (#141820) — cards, lists
 *   surfaceVariant = Surface3 (#1A1F2E) — inputs, chips, elevated
 *   surfaceContainerHigh = Surface4  — active elevated state
 *
 * Light mode surface hierarchy (v6.1 — Soft Violet):
 *   background  = BackgroundBaseLight (#EDE9FE) — Violet-100, clearly NOT white
 *   surface     = Surface2Light (#FFFFFF) — pure white cards floating on violet
 *   surfaceVariant = Surface3Light (#DDD6FE) — Violet-200 chip/input fill
 *
 * Primary action flow:
 *   dark:  primary = Indigo400 (#818CF8)
 *   light: primary = CampusBlue600 (#2563EB)
 */
package com.abpvt.campusgig_frontend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Dark Color Scheme ────────────────────────────────────────────────────────
private val CampusVaultDarkColorScheme = darkColorScheme(
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

// ─── Light Color Scheme — LinkedIn Warm Beige + Duolingo Multi-Color (v6.0) ──
private val CampusVaultLightColorScheme = lightColorScheme(
    // ── Primary: Electric Indigo (#4F46E5) — Premium AI/Tech (Linear, Vercel style)
    primary             = IndigoLight600,         // #4F46E5 — Electric Indigo
    onPrimary           = Color.White,
    primaryContainer    = IndigoLight100,         // #EEF2FF — soft indigo chip fill
    onPrimaryContainer  = IndigoLight700,         // #4338CA — deep indigo text

    // ── Secondary: Zomato Coral-Red — Energy, Discovery, Trending
    secondary           = CoralRed500,            // #E23744 — Zomato Coral Red
    onSecondary         = Color.White,
    secondaryContainer  = CoralRed100,            // #FFF0F3 — soft rose chip fill
    onSecondaryContainer = CoralRed600,

    // ── Tertiary: Forest Emerald — Success, Open, Applications, Joined
    tertiary            = Emerald500,             // #059669 — Forest Emerald
    onTertiary          = Color.White,
    tertiaryContainer   = Emerald100,             // #D1FAE5 — soft green chip fill
    onTertiaryContainer = Emerald700,

    // ── Surfaces: LinkedIn Warm Beige-Gray — clearly NOT white, rich & cozy
    background          = BackgroundBaseLight,    // #F3F2EF — LinkedIn Warm Beige
    onBackground        = TextPrimaryLight,       // #1A1A2E — Swiggy Deep Navy

    surface             = Surface2Light,          // #FFFFFF — crisp white cards on beige
    onSurface           = TextPrimaryLight,       // #1A1A2E

    surfaceVariant      = Surface3Light,          // #EBF0F7 — blue-tinted chip fill
    onSurfaceVariant    = TextSecondaryLight,     // #6B7280 — Notion Slate

    surfaceContainerHighest = Surface4Light,
    surfaceContainer    = Surface3Light,
    surfaceContainerHigh= Surface4Light,
    surfaceContainerLow = Surface2Light,
    surfaceContainerLowest = BackgroundBaseLight,

    // ── Outlines: Notion-style subtle + blue-tinted active
    outline             = BorderSubtleLight,      // #E0E0E0 — soft card border
    outlineVariant      = BorderDefaultLight,     // #C0C8D8 — active focus border

    // ── Error
    error               = ErrorColorLight,        // #DC2626
    onError             = Color.White,
    errorContainer      = PastelOrange,
    onErrorContainer    = ErrorColorLight,

    // ── Inverse
    inverseSurface      = TextPrimaryLight,
    inverseOnSurface    = Surface2Light,
    inversePrimary      = IndigoLight400,

    scrim               = Color(0x80000000)
)

/**
 * CampusGigTheme — Root composable that wraps the entire app in Material3 theming.
 *
 * @param darkTheme  Whether to use dark mode. Pass from ThemeViewModel for user control.
 * @param content    The composable content to theme.
 */
@Composable
fun CampusGigTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CampusVaultDarkColorScheme else CampusVaultLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}

// Backward-compat aliases
@Composable
fun CampusVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = CampusGigTheme(darkTheme = darkTheme, content = content)

@Composable
fun Campusgig_frontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = CampusGigTheme(darkTheme = darkTheme, content = content)