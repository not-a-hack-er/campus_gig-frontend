package com.abpvt.campusgig_frontend.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abpvt.campusgig_frontend.ui.theme.BackgroundBase
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.Violet500

/**
 * VLockLogoIcon — Official CampusVault Brand Mark.
 * Geometric V with keyhole negative space, rendered on Compose Canvas.
 * Used in the sticky top bar, splash screen, and headers.
 */
@Composable
fun VLockLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 30.dp
) {
    val cornerRadius = size * 0.25f
    Box(
        modifier = modifier
            .size(size)
            .background(color = BackgroundBase, shape = RoundedCornerShape(cornerRadius))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        val canvasSize = size * 0.58f
        Canvas(modifier = Modifier.size(canvasSize)) {
            val w = this.size.width
            val h = this.size.height

            val vGradient = Brush.linearGradient(
                colors = listOf(GradientIndigoStart, GradientIndigoMid, Violet500),
                start = Offset(0f, 0f),
                end   = Offset(w, h)
            )

            val armThickness = w * 0.22f
            val vertexY     = h * 0.80f
            val topInset    = 0f

            val vPath = Path().apply {
                moveTo(0f, topInset)
                lineTo(armThickness, topInset)
                lineTo(w * 0.5f, vertexY)
                lineTo(w - armThickness, topInset)
                lineTo(w, topInset)
                lineTo(w * 0.5f, vertexY + armThickness * 0.5f)
                close()
            }

            val khRadius = w * 0.085f
            val khCx     = w * 0.5f
            val khCy     = vertexY - khRadius * 0.4f
            val slotW    = khRadius * 0.7f
            val slotH    = khRadius * 1.6f

            val keyholePath = Path().apply {
                addOval(Rect(center = Offset(khCx, khCy), radius = khRadius))
                addRect(
                    Rect(
                        offset = Offset(khCx - slotW / 2f, khCy + khRadius * 0.55f),
                        size   = Size(slotW, slotH)
                    )
                )
            }

            val markPath = Path().apply {
                op(vPath, keyholePath, PathOperation.Difference)
            }

            drawPath(path = markPath, brush = vGradient)
        }
    }
}
