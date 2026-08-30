/**
 * SlideToActionButton.kt — Rapido / Uber Style Swipe Slider Button
 *
 * Provides a tactile, satisfying "Slide to Confirm / Submit" gesture,
 * inspired by Rapido, Swiggy, and iOS lock screen sliders.
 */
package com.abpvt.campusgig_frontend.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.Violet500
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SlideToActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isCompleted: Boolean = false,
    gradientColors: List<Color> = listOf(GradientIndigoStart, GradientIndigoMid, Violet500),
    onSwipeComplete: () -> Unit
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (isCompleted) SemanticSuccess.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    if (isCompleted) listOf(SemanticSuccess, SemanticSuccess)
                    else listOf(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        val thumbSizePx = with(density) { 48.dp.toPx() }
        val paddingPx = with(density) { 4.dp.toPx() }
        val maxDragPx = with(density) { maxWidth.toPx() } - thumbSizePx - (paddingPx * 2)

        // Progress fill track behind the thumb
        val currentProgress = if (maxDragPx > 0) (offsetX.value / maxDragPx).coerceIn(0f, 1f) else 0f
        val fillWidthDp = with(density) { (offsetX.value + thumbSizePx + paddingPx).toDp() }

        if (currentProgress > 0f) {
            Box(
                modifier = Modifier
                    .width(fillWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.horizontalGradient(gradientColors.map { it.copy(alpha = 0.35f) })
                    )
            )
        }

        // Centered shimmer text label
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCompleted) "Submitted ✓" else text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                ),
                color = if (isCompleted) SemanticSuccess
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = (1f - (currentProgress * 0.7f)).coerceIn(0.3f, 1f))
            )
            if (!isCompleted) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "› › ›",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = (1f - currentProgress).coerceIn(0.2f, 1f))
                )
            }
        }

        // Draggable Floating Thumb Icon
        Box(
            modifier = Modifier
                .padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    brush = if (isCompleted) {
                        Brush.linearGradient(listOf(SemanticSuccess, SemanticSuccess))
                    } else {
                        Brush.linearGradient(gradientColors)
                    }
                )
                .pointerInput(enabled, isCompleted) {
                    if (!enabled || isCompleted) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value >= maxDragPx * 0.82f) {
                                    // Complete drag
                                    offsetX.animateTo(
                                        targetValue = maxDragPx,
                                        animationSpec = spring(stiffness = Spring.StiffnessHigh)
                                    )
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSwipeComplete()
                                } else {
                                    // Reset drag
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newX = (offsetX.value + dragAmount).coerceIn(0f, maxDragPx)
                                offsetX.snapTo(newX)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Swipe Handle",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
