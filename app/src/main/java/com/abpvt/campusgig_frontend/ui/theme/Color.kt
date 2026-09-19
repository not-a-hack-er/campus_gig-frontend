/**
 * CampusGig Color System — v6.0 (LinkedIn + Zomato + Duolingo Inspired)
 *
 * Research-backed design:
 * - BACKGROUND: Soft Violet (#EDE9FE) — Violet-100, clearly NOT white, pairs perfectly with Indigo primary
 * - CARD SURFACE: Pure White (#FFFFFF) floating on violet — creates stunning depth
 * - PRIMARY: Electric Indigo (#4F46E5) — premium AI/tech primary
 * - SECONDARY: Zomato Coral-Red (#E23744) — high energy, discovery, trending
 * - TERTIARY: Forest Emerald (#059669) — success, open, joined
 * - CATEGORY SYSTEM: 8 distinct rich Duolingo-style colors
 * - TYPOGRAPHY: Swiggy Deep Navy (#1A1A2E) headings, Notion Slate (#6B7280) body
 */
package com.abpvt.campusgig_frontend.ui.theme

import androidx.compose.ui.graphics.Color

// ─── CampusVault Cyber-Industrial Workshop tokens ────────────────────────────
// These mirror the website. Existing aliases below point here so the feature
// screens share one visual language without changing their behavior.
val CvBg0       = Color(0xFF1D1A17)
val CvBg1       = Color(0xFF24201D)
val CvBg2       = Color(0xFF2C2824)
val CvBg3       = Color(0xFF36312B)
val CvMetal0    = Color(0xFF302B25)
val CvMetal1    = Color(0xFF403932)
val CvMetal2    = Color(0xFF5A5146)
val CvMetalEdge = Color(0xFF7B7061)
val CvPrimary   = Color(0xFF22C55E)
val CvPrimarySoft = Color(0xFF4ADE80)
val CvPrimaryDeep = Color(0xFF15803D)
val CvMint      = Color(0xFF5DB89A)
val CvMintSoft  = Color(0xFF7FC9AC)
val CvMintDeep  = Color(0xFF2D8A6E)
val CvCoral     = Color(0xFFFB7185)
val CvCoralSoft = Color(0xFFFDA4AF)
val CvText0     = Color(0xFFF5F0E7)
val CvText1     = Color(0xFFD8D0C4)
val CvText2     = Color(0xFFB0A89C)
val CvText3     = Color(0xFF9E968A)

// ─── Dark Mode Surfaces ────────────────────────────────────────────────────────
val BackgroundBase     = CvBg0
val Surface1           = CvBg0
val Surface2           = CvBg1
val Surface3           = CvBg2
val Surface4           = CvBg3
val BorderSubtle       = CvMetal1
val BorderDefault      = CvMetal2

// ─── Light Mode Surfaces — Soft Violet / Lavender (v6.1) ─────────────────────
val BackgroundBaseLight  = Color(0xFFFAF8F5)
val Surface1Light        = Color(0xFFF3EEE4)
val Surface2Light        = Color(0xFFFFFFFF)
val Surface3Light        = Color(0xFFF3EEE4)
val Surface4Light        = Color(0xFFE6DFD1)

val BorderSubtleLight    = Color(0xFFCFC6B6)
val BorderDefaultLight   = Color(0xFFA89F91)

// ─── Primary — CampusVault phosphor green ─────────────────────────────────────
val IndigoLight100     = Color(0xFFE8F7EC)
val IndigoLight200     = Color(0xFFC7EFD2)
val IndigoLight400     = Color(0xFF22C55E)
val IndigoLight500     = Color(0xFF16A34A)
val IndigoLight600     = Color(0xFF16A34A)
val IndigoLight700     = Color(0xFF15803D)

// Dark mode Indigo aliases (kept for backward compat)
val Indigo400          = CvPrimarySoft
val Indigo500          = CvPrimary
val Indigo600          = CvPrimary
val Indigo700          = CvPrimaryDeep

// ─── Secondary — Zomato Coral-Red (Energy, Trending, Discovery) ──────────────
val CoralRed100        = Color(0xFFFFF0F3)  // Soft rose chip fill
val CoralRed400        = Color(0xFFF16F7A)  // Icon on white
val CoralRed500        = Color(0xFFE23744)  // ZOMATO CORAL-RED — secondary
val CoralRed600        = Color(0xFFCB202D)  // Deep Zomato Red — pressed
val CoralRed700        = Color(0xFFB71C1C)  // Darkest

// Legacy alias
val CampusCoral400     = CoralRed400
val CampusCoral500     = CoralRed500
val CampusCoral600     = CoralRed600
val CampusCoral700     = CoralRed700
val CampusCoral100     = CoralRed100

// ─── Tertiary — Forest Emerald (Success, Open, Joined) ────────────────────────
val Emerald100         = Color(0xFFD1FAE5)  // Soft green chip fill
val Emerald500         = Color(0xFF059669)  // FOREST EMERALD — tertiary / success
val Emerald600         = Color(0xFF047857)  // Pressed state
val Emerald700         = Color(0xFF065F46)  // Darkest

// ─── Legacy accent aliases — retained for compatibility, aligned to the brand ──
val Violet100          = Color(0xFFE8F7EC)
val Violet400          = CvPrimarySoft
val Violet500          = CvPrimary
val Violet600          = CvPrimaryDeep

// ─── Amber (Ratings, Stars, Warnings) ─────────────────────────────────────────
val Amber100           = Color(0xFFFFFBEB)
val Amber400           = Color(0xFFFCD34D)
val Amber500           = Color(0xFFF59E0B)
val Amber600           = Color(0xFFD97706)

// ─── Sky Blue (Video, Tech) ────────────────────────────────────────────────────
val SkyBlue100         = Color(0xFFE0F2FE)
val SkyBlue500         = Color(0xFF0284C7)
val SkyBlue600         = Color(0xFF0369A1)

// ─── Swiggy Orange (Marketing, Trending) ──────────────────────────────────────
val SwiggyOrange100    = Color(0xFFFFF7ED)
val SwiggyOrange500    = Color(0xFFEA580C)
val SwiggyOrange400    = Color(0xFFF97316)

// ─── Grape Purple (Photography, Creative) ─────────────────────────────────────
val GrapePurple100     = Color(0xFFFDF4FF)
val GrapePurple500     = Color(0xFF9333EA)
val GrapePurple400     = Color(0xFFA855F7)

// ─── Gradient Tokens ──────────────────────────────────────────────────────────
// Dark Mode Primary Gradient
val GradientIndigoStart   = CvPrimary
val GradientIndigoMid     = Color(0xFF1FA653)
val GradientIndigoEnd     = CvPrimaryDeep

// Gold & Teal Gradients (dark mode communities / status)
val GradientGoldStart     = Color(0xFFF59E0B)
val GradientGoldEnd       = Color(0xFFFBBF24)
val GradientTealStart     = Color(0xFF0D9488)
val GradientTealEnd       = Color(0xFF06B6D4)

// Light Mode Primary Gradient — phosphor green to deep green
val GradientLightStart    = Color(0xFF22C55E)
val GradientLightMid      = Color(0xFF16A34A)
val GradientLightEnd      = Color(0xFF15803D)

// Light Mode Featured Card Gradients — restrained CampusVault workshop tones
val GradientLightCoding    = listOf(Color(0xFFE5F3E9), Color(0xFFBFE8CB))
val GradientLightDesign    = listOf(Color(0xFFF8E8E9), Color(0xFFF0C9CD))
val GradientLightWriting   = listOf(Color(0xFFE2F1EA), Color(0xFFC2E2D3))
val GradientLightTutoring  = listOf(Color(0xFFF6ECDC), Color(0xFFEAD5A9))
val GradientLightPhoto     = listOf(Color(0xFFF5E6DF), Color(0xFFEAC3B3))
val GradientLightVideo     = listOf(Color(0xFFE3EEEE), Color(0xFFC7DDDF))
val GradientLightMarketing = listOf(Color(0xFFF1F0DC), Color(0xFFDEDBA3))
val GradientLightOther     = listOf(Color(0xFFEDE8E0), Color(0xFFD6CCBC))

// Glow tokens
val GlowIndigo            = Color(0x3322C55E)
val GlowOrange            = Color(0x33FB7185)

// ─── Soft Pastel Category Backgrounds ─────────────────────────────────────────
val PastelBlue     = Color(0xFFEEF2FF)   // Coding — Indigo tint
val PastelOrange   = Color(0xFFFFF0F3)   // Design — Coral-Rose tint
val PastelGreen    = Color(0xFFD1FAE5)   // Writing — Emerald tint
val PastelAmber    = Color(0xFFFFFBEB)   // Tutoring — Amber tint
val PastelPink     = Color(0xFFFDF4FF)   // Photography — Purple tint
val PastelCyan     = Color(0xFFE0F2FE)   // Video — Sky Blue tint
val PastelRed      = Color(0xFFFFF7ED)   // Marketing — Swiggy Orange tint
val PastelPurple   = Color(0xFFF5F3FF)   // Other — Violet tint

// ─── Semantic Colors ──────────────────────────────────────────────────────────
val SemanticSuccess        = CvPrimary
val SemanticSuccessBg      = Color(0x1F22C55E)
val SemanticWarning        = Color(0xFFD97706)   // Amber
val SemanticWarningBg      = Color(0x1FD97706)
val SemanticError          = CvCoral
val SemanticErrorBg        = Color(0x1FFB7185)
val SemanticInfo           = CvMint
val SemanticInfoBg         = Color(0x1F5DB89A)

// ─── Text Colors (Dark Mode) ──────────────────────────────────────────────────
val TextPrimary            = CvText0
val TextSecondary          = CvText1
val TextTertiary           = CvText2
val TextInverted           = CvBg0

// ─── Text Colors (Light Mode — Research-Backed) ───────────────────────────────
val TextPrimaryLight       = Color(0xFF1C1917)
val TextSecondaryLight     = Color(0xFF292524)
val TextTertiaryLight      = Color(0xFF57534E)

// ─── Legacy Aliases (backward compat) ─────────────────────────────────────────
val BackgroundDark         = Surface1
val SurfaceDark            = Surface2
val SurfaceVariantDark     = Surface4
val DividerDark            = BorderSubtle
val BackgroundLight        = BackgroundBaseLight
val SurfaceLight           = Surface1Light
val SurfaceVariantLight    = Surface3Light
val TextPrimaryDark        = TextPrimary
val TextSecondaryDark      = TextSecondary
val CampusIndigo80         = Violet400
val CampusIndigo60         = Indigo400
val CampusIndigo40         = Indigo500
val CampusIndigo20         = Indigo600
val CampusIndigoDark       = Indigo700
val CampusTeal80           = Color(0xFF99F6E4)
val CampusTeal40           = GradientTealEnd
val CampusTeal20           = GradientTealStart
val GradientStart          = GradientLightStart
val GradientMid            = GradientLightMid
val GradientEnd            = GradientLightEnd
val CampusAmber            = SemanticWarning
val CampusAmberLight       = Amber400
val CampusAmberDark        = Amber600
val ErrorColor             = SemanticError
val ErrorColorLight        = Color(0xFFDC2626)
val OnErrorColor           = Color.White
val SuccessColor           = SemanticSuccess
val WarningColor           = SemanticWarning
val StatusPending          = SemanticWarning
val StatusAccepted         = SemanticSuccess
val StatusRejected         = SemanticError
val StatusInProgress       = IndigoLight600
val StatusCompleted        = SemanticSuccess
val ScrimColor             = Color(0xB3000000)
val CardOverlay            = Color(0x0DFFFFFF)
