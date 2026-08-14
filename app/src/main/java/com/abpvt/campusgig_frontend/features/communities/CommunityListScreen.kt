/**
 * CommunityListScreen.kt — Discover Communities (Placeholder / Coming Soon)
 *
 * This screen displays a clean, premium "Coming Soon" card for the Campus Community Hub.
 */
package com.abpvt.campusgig_frontend.features.communities

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.Indigo400
import com.abpvt.campusgig_frontend.ui.theme.Indigo500
import com.abpvt.campusgig_frontend.ui.theme.Surface1
import com.abpvt.campusgig_frontend.ui.theme.Surface2
import com.abpvt.campusgig_frontend.ui.theme.TextPrimary
import com.abpvt.campusgig_frontend.ui.theme.TextSecondary

@Composable
fun CommunityListScreen(navController: NavController) {
    // Pulsing animation for the decorative glow element
    val infiniteTransition = rememberInfiniteTransition(label = "comingSoonPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface1),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background gradient sphere
        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Indigo500.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon wrapper with premium gradient border
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Surface2)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = Indigo400,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium animated badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(GradientIndigoStart.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        color = Indigo400.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(99.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "COMING SOON",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = Indigo400
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Campus Community Hub",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We are building a collaborative workspace for students to network, share opportunities, form study groups, and discuss projects. Stay tuned for a launch soon!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Back button
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Go Back",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
            }
        }
    }
}
