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
 *   dark:  primary = CampusVault phosphor green
 *   light: primary = CampusVault deep green
 */
package com.abpvt.campusgig_frontend.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Dark Color Scheme ────────────────────────────────────────────────────────
private val CampusVaultDarkColorScheme = darkColorScheme(
    primary             = CvPrimary,
    onPrimary           = CvBg0,
    primaryContainer    = CvPrimaryDeep,
    onPrimaryContainer  = CvPrimarySoft,

    secondary           = CvMint,
    onSecondary         = CvBg0,
    secondaryContainer  = Color(0xFF245E4C),
    onSecondaryContainer = CvMintSoft,

    tertiary            = CvCoral,
    onTertiary          = CvBg0,
    tertiaryContainer   = Color(0xFF5B2932),
    onTertiaryContainer = CvCoralSoft,

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
    inversePrimary      = CvPrimary,

    scrim               = ScrimColor
)

// ─── Light Color Scheme — LinkedIn Warm Beige + Duolingo Multi-Color (v6.0) ──
private val CampusVaultLightColorScheme = lightColorScheme(
    primary             = CvPrimaryDeep,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFFDDF7E6),
    onPrimaryContainer  = CvPrimaryDeep,

    secondary           = CvMintDeep,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFDCEFE8),
    onSecondaryContainer = CvMintDeep,

    // ── Tertiary: Forest Emerald — Success, Open, Applications, Joined
    tertiary            = CvCoral,
    onTertiary          = Color.White,
    tertiaryContainer   = Emerald100,             // #D1FAE5 — soft green chip fill
    onTertiaryContainer = Emerald700,

    // ── Surfaces: warm CampusVault paper/metal tones
    background          = BackgroundBaseLight,
    onBackground        = TextPrimaryLight,

    surface             = Surface2Light,
    onSurface           = TextPrimaryLight,

    surfaceVariant      = Surface3Light,
    onSurfaceVariant    = TextSecondaryLight,

    surfaceContainerHighest = Surface4Light,
    surfaceContainer    = Surface3Light,
    surfaceContainerHigh= Surface4Light,
    surfaceContainerLow = Surface2Light,
    surfaceContainerLowest = BackgroundBaseLight,

    // ── Outlines: warm metal-inspired neutrals
    outline             = BorderSubtleLight,
    outlineVariant      = BorderDefaultLight,

    // ── Error
    error               = ErrorColorLight,        // #DC2626
    onError             = Color.White,
    errorContainer      = PastelOrange,
    onErrorContainer    = ErrorColorLight,

    // ── Inverse
    inverseSurface      = TextPrimaryLight,
    inverseOnSurface    = Surface2Light,
    inversePrimary      = CvPrimary,

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
    darkTheme: Boolean = true,
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
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = CampusGigTheme(darkTheme = darkTheme, content = content)

@Composable
fun Campusgig_frontendTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = CampusGigTheme(darkTheme = darkTheme, content = content)
