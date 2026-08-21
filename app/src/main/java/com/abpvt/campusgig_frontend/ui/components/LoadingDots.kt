package com.abpvt.campusgig_frontend.ui.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abpvt.campusgig_frontend.ui.theme.CampusIndigo40
import kotlinx.coroutines.delay

/**
 * LoadingDots — A premium animated three-dot pulsing loading indicator.
 *
 * @param color The color of the dots. Defaults to CampusIndigo40.
 * @param dotSize The size of each dot. Defaults to 10.dp.
 * @param spacing Spacing between dots. Defaults to 6.dp.
 * @param modifier Optional layout modifier.
 */
@Composable
fun LoadingDots(
    modifier: Modifier = Modifier,
    color: Color = CampusIndigo40,
    dotSize: Dp = 10.dp,
    spacing: Dp = 6.dp
) {
    val animatables = listOf(
        remember { Animatable(0.4f) },
        remember { Animatable(0.4f) },
        remember { Animatable(0.4f) }
    )

    animatables.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 150L) // Stagger dots
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 450, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Row(
        modifier = modifier.padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        animatables.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(animatable.value)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}
