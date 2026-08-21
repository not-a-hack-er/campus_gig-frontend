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

// ─── Dark Mode Surfaces ────────────────────────────────────────────────────────
val BackgroundBase     = Color(0xFF0A0C12)  // Deepest background — app shell
val Surface1           = Color(0xFF0F1117)  // Main screen background
val Surface2           = Color(0xFF141820)  // Card backgrounds
val Surface3           = Color(0xFF1A1F2E)  // Elevated cards, modals, input fields
val Surface4           = Color(0xFF1F2535)  // Chips, active inputs
val BorderSubtle       = Color(0xFF2A2F42)  // Card borders, dividers
val BorderDefault      = Color(0xFF353B52)  // Input borders, active states

// ─── Light Mode Surfaces — Soft Violet / Lavender (v6.1) ─────────────────────
val BackgroundBaseLight  = Color(0xFFEDE9FE)  // Violet-100 — clearly NOT white, rich soft lavender
val Surface1Light        = Color(0xFFE8E2FD)  // Slightly deeper violet for headers/bars
val Surface2Light        = Color(0xFFFFFFFF)  // Pure crisp white cards floating on violet
val Surface3Light        = Color(0xFFDDD6FE)  // Violet-200 — chip/input fill, visible tint
val Surface4Light        = Color(0xFFC4B5FD)  // Violet-300 — active inputs

val BorderSubtleLight    = Color(0xFFD8D0FC)  // Violet-tinted card border — pairs with lavender bg
val BorderDefaultLight   = Color(0xFFB9A8FA)  // Deeper violet active input focus border

// ─── Primary — Electric Indigo (Premium 2024 AI/Tech Primary) ─────────────────
val IndigoLight100     = Color(0xFFEEF2FF)  // Chip fill background
val IndigoLight200     = Color(0xFFC7D2FE)  // Divider / subtle tint
val IndigoLight400     = Color(0xFF818CF8)  // Icon on white
val IndigoLight500     = Color(0xFF6366F1)  // Lighter shade
val IndigoLight600     = Color(0xFF4F46E5)  // LIGHT MODE PRIMARY — Electric Indigo
val IndigoLight700     = Color(0xFF4338CA)  // Pressed state

// Dark mode Indigo aliases (kept for backward compat)
val Indigo400          = Color(0xFF818CF8)
val Indigo500          = Color(0xFF6366F1)
val Indigo600          = Color(0xFF4F46E5)
val Indigo700          = Color(0xFF4338CA)

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

// ─── Violet (Communities, Creative, Special) ──────────────────────────────────
val Violet100          = Color(0xFFF5F3FF)  // Soft violet chip fill
val Violet400          = Color(0xFFA78BFA)
val Violet500          = Color(0xFF8B5CF6)
val Violet600          = Color(0xFF7C3AED)

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
val GradientIndigoStart   = Color(0xFF6366F1)
val GradientIndigoMid     = Color(0xFF8B5CF6)
val GradientIndigoEnd     = Color(0xFFA78BFA)

// Gold & Teal Gradients (dark mode communities / status)
val GradientGoldStart     = Color(0xFFF59E0B)
val GradientGoldEnd       = Color(0xFFFBBF24)
val GradientTealStart     = Color(0xFF0D9488)
val GradientTealEnd       = Color(0xFF06B6D4)

// Light Mode Primary Gradient — Electric Indigo to Violet
val GradientLightStart    = Color(0xFF4F46E5)   // Electric Indigo
val GradientLightMid      = Color(0xFF7C3AED)   // Violet
val GradientLightEnd      = Color(0xFF9333EA)   // Grape Purple

// Light Mode Featured Card Gradients — Duolingo-inspired, 8 distinct rich colors
val GradientLightCoding    = listOf(Color(0xFF4F46E5), Color(0xFF06B6D4))  // Indigo→Cyan
val GradientLightDesign    = listOf(Color(0xFFE23744), Color(0xFFF472B6))  // Coral→Pink
val GradientLightWriting   = listOf(Color(0xFF059669), Color(0xFF10B981))  // Emerald→Mint
val GradientLightTutoring  = listOf(Color(0xFFD97706), Color(0xFFFBBF24))  // Amber→Gold
val GradientLightPhoto     = listOf(Color(0xFF9333EA), Color(0xFFEC4899))  // Purple→Pink
val GradientLightVideo     = listOf(Color(0xFF0284C7), Color(0xFF06B6D4))  // SkyBlue→Cyan
val GradientLightMarketing = listOf(Color(0xFFEA580C), Color(0xFFF59E0B))  // Swiggy Orange→Amber
val GradientLightOther     = listOf(Color(0xFF7C3AED), Color(0xFF4F46E5))  // Violet→Indigo

// Glow tokens
val GlowIndigo            = Color(0x334F46E5)  // Electric Indigo ambient
val GlowOrange            = Color(0x33E23744)  // Coral ambient

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
val SemanticSuccess        = Color(0xFF059669)   // Forest Emerald
val SemanticSuccessBg      = Color(0x1F059669)
val SemanticWarning        = Color(0xFFD97706)   // Amber
val SemanticWarningBg      = Color(0x1FD97706)
val SemanticError          = Color(0xFFDC2626)   // Red
val SemanticErrorBg        = Color(0x1FDC2626)
val SemanticInfo           = Color(0xFF0284C7)   // Sky Blue
val SemanticInfoBg         = Color(0x1F0284C7)

// ─── Text Colors (Dark Mode) ──────────────────────────────────────────────────
val TextPrimary            = Color(0xFFF1F5F9)
val TextSecondary          = Color(0xFF94A3B8)
val TextTertiary           = Color(0xFF64748B)
val TextInverted           = Color(0xFF1A1A2E)

// ─── Text Colors (Light Mode — Research-Backed) ───────────────────────────────
val TextPrimaryLight       = Color(0xFF1A1A2E)  // Swiggy Deep Navy Charcoal
val TextSecondaryLight     = Color(0xFF6B7280)  // Notion Slate Gray
val TextTertiaryLight      = Color(0xFF9CA3AF)  // Soft hint gray

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