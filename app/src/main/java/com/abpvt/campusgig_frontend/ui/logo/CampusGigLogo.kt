/**
 * CampusGigLogo.kt — Dedicated Brand Logo & Emblem Package
 *
 * Location: com.abpvt.campusgig_frontend.ui.logo.CampusGigLogo.kt
 *
 * Provides all official CampusGig brand logos, icons, badges, and wordmarks
 * for easy access, customization, and reuse throughout the application.
 */
package com.abpvt.campusgig_frontend.ui.logo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart

/**
 * CampusGigLogoIcon — The official Brand Icon Tile.
 * Deep Obsidian Squircle containing the glowing dual-beam Violet "V" emblem.
 */
@Composable
fun CampusGigLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp
) {
    val cornerRadius = size * 0.28f
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = Color(0xFF0D0D14),
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color(0xFF8B5CF6).copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        val canvasSize = size * 0.60f
        Canvas(modifier = Modifier.size(canvasSize)) {
            val w = this.size.width
            val h = this.size.height

            val violetBeamBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF7C3AED), // Deep violet
                    Color(0xFF8B5CF6), // Electric violet
                    Color(0xFFC084FC)  // Light glowing purple
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )

            val strokeW = w * 0.21f

            // Left Beam of "V"
            drawLine(
                brush = violetBeamBrush,
                start = Offset(w * 0.28f, h * 0.18f),
                end = Offset(w * 0.44f, h * 0.70f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Right Beam of "V"
            drawLine(
                brush = violetBeamBrush,
                start = Offset(w * 0.72f, h * 0.18f),
                end = Offset(w * 0.56f, h * 0.70f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Center Convergence Glowing Node Dot
            drawCircle(
                color = Color(0xFFA855F7),
                radius = w * 0.085f,
                center = Offset(w * 0.50f, h * 0.76f)
            )
        }
    }
}

/**
 * CampusGigHeaderLogo — Full Brand Logo with Icon + "CampusGig" Wordmark.
 */
@Composable
fun CampusGigHeaderLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    fontSize: Float = 20f
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CampusGigLogoIcon(size = iconSize)
        Spacer(modifier = Modifier.width(10.dp))
        CampusGigWordmark(fontSize = fontSize)
    }
}

/**
 * CampusGigWordmark — Styled "CampusGig" text.
 */
@Composable
fun CampusGigWordmark(
    modifier: Modifier = Modifier,
    fontSize: Float = 20f
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            ) { append("Campus") }
            withStyle(
                SpanStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GradientIndigoStart, GradientIndigoEnd)
                    ),
                    fontWeight = FontWeight.ExtraBold
                )
            ) { append("Gig") }
        },
        modifier = modifier,
        style = TextStyle(
            fontSize = fontSize.sp,
            letterSpacing = (-0.5).sp
        )
    )
}

/** Backward compatibility alias */
@Composable
fun VLockLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp
) = CampusGigLogoIcon(modifier = modifier, size = size)
