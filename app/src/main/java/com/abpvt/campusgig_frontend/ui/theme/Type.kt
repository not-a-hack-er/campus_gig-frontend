/**
 * CampusGig Typography System — v2.0
 *
 * Font: Inter (via system default — closely matches Inter on Android)
 * Scale follows the design spec with precise sizes, weights, and letter-spacings.
 *
 * Scale:
 * displayLarge  → Display XL: 40sp/700/-0.5  (splash, onboarding headlines)
 * displayMedium → Display L:  32sp/700/-0.3
 * displaySmall  → Heading 1:  28sp/600/-0.2  (screen titles)
 * headlineLarge → Heading 2:  22sp/600/-0.1  (section headers)
 * headlineMedium→ Heading 3:  18sp/600/0     (card titles)
 * headlineSmall → Body Large: 16sp/400/0     (primary body)
 * titleLarge    → 20sp/600   (used for prominent labels)
 * titleMedium   → 16sp/500   (sub-section titles)
 * titleSmall    → 14sp/500
 * bodyLarge     → 16sp/400/0 (primary body text)
 * bodyMedium    → 14sp/400/0 (secondary body, descriptions)
 * bodySmall     → 13sp/400/0 (meta text)
 * labelLarge    → 14sp/600   (buttons)
 * labelMedium   → 12sp/500/0.2 (caption — timestamps, small labels)
 * labelSmall    → 11sp/500/0.5/CAPS-ready (badges, tags)
 */
package com.abpvt.campusgig_frontend.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // ── Display — Splash & Onboarding Hero Text ───────────────────────────────
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.3).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.2).sp
    ),

    // ── Headlines — Section headers, screen subtitles ─────────────────────────
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.1).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    // ── Titles — Card headers, tab labels, prominent labels ───────────────────
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    // ── Body — Descriptions, content, form text ───────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    // ── Labels — Buttons, badges, tags, captions ──────────────────────────────
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)