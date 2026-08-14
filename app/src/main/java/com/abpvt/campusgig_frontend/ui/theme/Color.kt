/**
 * CampusGig Color System — v2.0
 *
 * Design Philosophy:
 * Intersection of LinkedIn's trust, Fiverr's marketplace energy, Linear's precision,
 * Notion's calm productivity, and Discord's community warmth.
 *
 * Architecture:
 * - SURFACES: Dark layered system (4 levels) for depth & hierarchy
 * - PRIMARY: Electric Indigo — main brand action color
 * - SECONDARY: Violet — supporting actions, gradients
 * - GRADIENTS: Hero CTAs, featured cards, onboarding
 * - SEMANTIC: Success/Warning/Error/Info with bg tints
 * - TEXT: 3-level hierarchy (Primary / Secondary / Tertiary)
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

// ─── Light Mode Surfaces ───────────────────────────────────────────────────────
val BackgroundBaseLight  = Color(0xFFF4F6FB)
val Surface1Light        = Color(0xFFFFFFFF)
val Surface2Light        = Color(0xFFF8F9FE)
val Surface3Light        = Color(0xFFEEF1F8)
val BorderSubtleLight    = Color(0xFFE2E6F0)
val BorderDefaultLight   = Color(0xFFC8CEDE)

// ─── Primary Accent — Electric Indigo ────────────────────────────────────────
val Indigo400          = Color(0xFF818CF8)  // Primary actions in dark mode
val Indigo500          = Color(0xFF6366F1)  // Primary brand color
val Indigo600          = Color(0xFF4F46E5)  // Primary in light mode
val Indigo700          = Color(0xFF4338CA)  // Pressed / active states

// ─── Secondary Accent — Violet ────────────────────────────────────────────────
val Violet400          = Color(0xFFA78BFA)
val Violet500          = Color(0xFF8B5CF6)
val Violet600          = Color(0xFF7C3AED)

// ─── Gradient Tokens ──────────────────────────────────────────────────────────
// Primary Gradient: use on CTAs, headers, FABs
val GradientIndigoStart   = Color(0xFF6366F1)
val GradientIndigoMid     = Color(0xFF8B5CF6)
val GradientIndigoEnd     = Color(0xFFA78BFA)

// Gold Gradient: premium / featured / opportunity cards
val GradientGoldStart     = Color(0xFFF59E0B)
val GradientGoldEnd       = Color(0xFFFBBF24)

// Teal Gradient: communities section
val GradientTealStart     = Color(0xFF14B8A6)
val GradientTealEnd       = Color(0xFF06B6D4)

// Glow: radial ambient behind hero elements
val GlowIndigo            = Color(0x266366F1)  // rgba(99,102,241,0.15)
val GlowIndigoStrong      = Color(0x4D6366F1)  // rgba(99,102,241,0.30)

// ─── Semantic Colors ──────────────────────────────────────────────────────────
val SemanticSuccess        = Color(0xFF10B981)
val SemanticSuccessBg      = Color(0x1F10B981)  // rgba(16,185,129,0.12)
val SemanticWarning        = Color(0xFFF59E0B)
val SemanticWarningBg      = Color(0x1FF59E0B)  // rgba(245,158,11,0.12)
val SemanticError          = Color(0xFFEF4444)
val SemanticErrorBg        = Color(0x1FEF4444)  // rgba(239,68,68,0.12)
val SemanticInfo           = Color(0xFF3B82F6)
val SemanticInfoBg         = Color(0x1F3B82F6)  // rgba(59,130,246,0.12)

// ─── Text Colors (dark mode) ──────────────────────────────────────────────────
val TextPrimary            = Color(0xFFF1F5F9)  // Headings, important text
val TextSecondary          = Color(0xFF94A3B8)  // Body text, descriptions
val TextTertiary           = Color(0xFF64748B)  // Placeholders, hints
val TextInverted           = Color(0xFF0F1117)  // Text on light backgrounds

// ─── Legacy aliases (kept for backward compat with screens not yet redesigned) ─
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
val GradientStart          = GradientIndigoStart
val GradientMid            = GradientIndigoMid
val GradientEnd            = GradientIndigoEnd
val CampusAmber            = SemanticWarning
val CampusAmberLight       = GradientGoldEnd
val CampusAmberDark        = Color(0xFFD97706)
val ErrorColor             = SemanticError
val ErrorColorLight        = Color(0xFFDC2626)
val OnErrorColor           = Color.White
val SuccessColor           = SemanticSuccess
val WarningColor           = SemanticWarning
val StatusPending          = SemanticWarning
val StatusAccepted         = SemanticSuccess
val StatusRejected         = SemanticError
val StatusInProgress       = Indigo500
val StatusCompleted        = SemanticSuccess
val ScrimColor             = Color(0xB3000000)
val CardOverlay            = Color(0x0DFFFFFF)