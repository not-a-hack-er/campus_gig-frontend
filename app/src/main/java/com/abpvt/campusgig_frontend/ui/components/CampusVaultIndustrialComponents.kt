package com.abpvt.campusgig_frontend.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abpvt.campusgig_frontend.ui.theme.CvBg0
import com.abpvt.campusgig_frontend.ui.theme.CvBg1
import com.abpvt.campusgig_frontend.ui.theme.CvBg2
import com.abpvt.campusgig_frontend.ui.theme.CvMetal2
import com.abpvt.campusgig_frontend.ui.theme.CvMetalEdge
import com.abpvt.campusgig_frontend.ui.theme.CvPrimary
import com.abpvt.campusgig_frontend.ui.theme.CvPrimaryDeep
import com.abpvt.campusgig_frontend.ui.theme.CvPrimarySoft
import com.abpvt.campusgig_frontend.ui.theme.CvText0
import com.abpvt.campusgig_frontend.ui.theme.CvText3

/** Shared primitives matching the CampusVault website's tactile terminal UI. */
@Composable
fun TactileButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .97f else 1f, label = "tactileScale")
    val view = LocalView.current
    val shape = RoundedCornerShape(12.dp)
    val colors = if (primary) {
        listOf(MaterialTheme.colorScheme.primary, CvPrimaryDeep)
    } else {
        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
    }
    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .border(1.dp, if (primary) CvPrimarySoft else CvMetalEdge, shape)
            .background(Brush.verticalGradient(colors), shape)
            .clickable(enabled = enabled, interactionSource = interaction, indication = null) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 18.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text.uppercase(), fontFamily = FontFamily.Monospace, fontSize = 12.sp,
            letterSpacing = 1.sp, fontWeight = FontWeight.Bold,
            color = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun LedIndicator(color: Color = CvPrimary, size: Dp = 7.dp, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "led")
    val alpha by transition.animateFloat(1f, .4f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ledAlpha")
    Box(modifier.size(size).clip(CircleShape).background(color.copy(alpha = alpha)).border(1.dp, Color.White.copy(.3f), CircleShape))
}

@Composable
fun MetalCard(
    modifier: Modifier = Modifier,
    tag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val colors = MaterialTheme.colorScheme
    Box(modifier.clip(shape).border(1.dp, colors.outline, shape)
        .background(Brush.verticalGradient(listOf(colors.surface, colors.surfaceVariant)), shape).padding(16.dp)) {
        Column {
            if (tag != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)) {
                LedIndicator()
                Text("[ $tag ]", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp, color = colors.onSurfaceVariant)
            }
            content()
        }
    }
}
