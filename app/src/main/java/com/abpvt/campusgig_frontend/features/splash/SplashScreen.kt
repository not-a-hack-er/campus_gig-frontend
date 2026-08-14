/**
 * SplashScreen.kt — Premium animated launch experience for CampusGig.
 *
 * Design inspiration: Linear, Stripe, Arc Browser.
 * - Dark obsidian background (#0B0F19) with radial glow orbs.
 * - Bouncing logo badge with spring scale-in entrance.
 * - Staggered fade-in for brand name and tagline.
 * - Animated floating gradient dots for depth.
 * - Auto-navigates after 2.2s to Login or Home (based on session).
 */
package com.abpvt.campusgig_frontend.features.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.BackgroundDark
import com.abpvt.campusgig_frontend.ui.theme.CampusIndigo40
import com.abpvt.campusgig_frontend.ui.theme.CampusIndigo80
import com.abpvt.campusgig_frontend.ui.theme.CampusTeal40
import com.abpvt.campusgig_frontend.ui.theme.CampusTeal80
import com.abpvt.campusgig_frontend.ui.theme.GradientEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientMid
import com.abpvt.campusgig_frontend.ui.theme.GradientStart
import com.abpvt.campusgig_frontend.ui.theme.TextSecondaryDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavController,
    isLoggedIn: Boolean
) {
    // ── Animation state ──────────────────────────────────────────────────────
    val logoScale   = remember { Animatable(0f) }
    val logoAlpha   = remember { Animatable(0f) }
    val titleAlpha  = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(24f) }
    val taglineAlpha = remember { Animatable(0f) }
    val screenAlpha  = remember { Animatable(1f) }

    // Infinite subtle pulse on the glow orbs
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue  = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )
    val dotFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_float"
    )

    // ── Orchestrated entrance sequence ───────────────────────────────────────
    LaunchedEffect(Unit) {
        // Step 1: Logo bounces in
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                )
            )
        }
        launch { logoAlpha.animateTo(1f, tween(400)) }

        // Step 2: Brand title slides up + fades in (staggered 300ms after logo)
        delay(300)
        launch {
            titleAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        launch {
            titleOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }

        // Step 3: Tagline fades in (staggered 500ms after title)
        delay(500)
        taglineAlpha.animateTo(1f, tween(600))

        // Step 4: Hold for a moment, then fade out entire screen
        delay(1000)
        screenAlpha.animateTo(
            targetValue  = 0f,
            animationSpec = tween(400, easing = LinearEasing)
        )

        // Step 5: Navigate to appropriate start destination
        val destination = if (isLoggedIn) Routes.HOME else Routes.LOGIN
        navController.navigate(destination) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // ── Background radial glow orbs ──────────────────────────────────────
        Box(
            modifier = Modifier
                .size((340 * orbPulse).dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-40).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CampusIndigo40.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size((300 * orbPulse).dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CampusTeal40.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ── Floating ambient dots ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(8.dp)
                .align(Alignment.TopStart)
                .offset(x = 60.dp, y = (160 + dotFloat).dp)
                .background(CampusIndigo40.copy(alpha = 0.4f), RoundedCornerShape(50))
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-80).dp, y = (-200 - dotFloat).dp)
                .background(CampusTeal40.copy(alpha = 0.5f), RoundedCornerShape(50))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (-40).dp, y = (-80 + dotFloat).dp)
                .background(CampusIndigo40.copy(alpha = 0.3f), RoundedCornerShape(50))
        )

        // ── Center content: Logo + Brand text ───────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Badge with spring scale-in
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .size(96.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientMid, GradientEnd)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.30f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎓",
                    fontSize = 46.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand name with gradient brush text + slide-up entrance
            Text(
                text = "CampusGig",
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffset.value.dp),
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(CampusIndigo80, CampusTeal80)
                    ),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline fade-in
            Text(
                text = "Learn · Earn · Collaborate",
                modifier = Modifier.alpha(taglineAlpha.value),
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondaryDark
            )
        }

        // ── Bottom version tag ───────────────────────────────────────────────
        Text(
            text = "v1.0 · Student Edition",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-32).dp)
                .alpha(taglineAlpha.value),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = Color.White.copy(alpha = 0.2f)
        )
    }
}
