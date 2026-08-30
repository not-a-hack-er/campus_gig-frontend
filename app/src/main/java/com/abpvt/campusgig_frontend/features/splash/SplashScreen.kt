/**
 * SplashScreen.kt — World-class animated launch experience for CampusGig.
 *
 * Design Inspiration: Linear, Stripe, Apple, Arc Browser.
 * - Deep obsidian radial canvas (#090D16) with dual pulsating ambient light orbs.
 * - Multi-layer glowing aura ring expanding behind the brand mark.
 * - Ultra-responsive spring physics entrance for CampusGig logo badge.
 * - Shimmering gradient sweep across the lightning bolt.
 * - Smooth staggered slide-up & fade-in for brand title & tagline.
 * - Seamless fade-out transition before entering the app.
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.logo.CampusGigLogoIcon
import com.abpvt.campusgig_frontend.ui.theme.BackgroundDark
import com.abpvt.campusgig_frontend.ui.theme.CampusIndigo40
import com.abpvt.campusgig_frontend.ui.theme.CampusTeal40
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.TextSecondaryDark
import com.abpvt.campusgig_frontend.ui.theme.Violet400
import com.abpvt.campusgig_frontend.ui.theme.Violet500
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavController,
    isLoggedIn: Boolean
) {
    // ── Entrance Animation State ─────────────────────────────────────────────
    val logoScale    = remember { Animatable(0.2f) }
    val logoAlpha    = remember { Animatable(0f) }
    val auraScale    = remember { Animatable(0.5f) }
    val auraAlpha    = remember { Animatable(0f) }
    val titleAlpha   = remember { Animatable(0f) }
    val titleOffset  = remember { Animatable(32f) }
    val taglineAlpha = remember { Animatable(0f) }
    val screenAlpha  = remember { Animatable(1f) }

    // ── Continuous Infinite Ambient Animations ──────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "splash_infinite")
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )
    val auraGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue  = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_glow"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    // ── Orchestrated Entrance Sequence ───────────────────────────────────────
    LaunchedEffect(Unit) {
        // Step 1: Glowing Aura expands behind logo
        launch {
            auraScale.animateTo(
                targetValue = 1.3f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
            )
        }
        launch { auraAlpha.animateTo(0.6f, tween(500)) }

        // Step 2: Bouncing Logo entrance with bouncy spring
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.55f, // Bouncy & playful
                    stiffness    = 280f
                )
            )
        }
        launch { logoAlpha.animateTo(1f, tween(350)) }

        // Step 3: Brand title slides up smoothly (staggered 250ms after logo)
        delay(250)
        launch {
            titleAlpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }
        launch {
            titleOffset.animateTo(0f, tween(450, easing = FastOutSlowInEasing))
        }

        // Step 4: Tagline & badges fade in (staggered 400ms after title)
        delay(400)
        taglineAlpha.animateTo(1f, tween(500))

        // Step 5: Hold screen briefly to wow user, then smooth fade-out exit
        delay(1100)
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(380, easing = LinearEasing)
        )

        // Step 6: Navigate to Login or Home
        val destination = if (isLoggedIn) Routes.HOME else Routes.LOGIN
        navController.navigate(destination) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    // ── Canvas Layout ────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(Color(0xFF090D16)),
        contentAlignment = Alignment.Center
    ) {
        // ── 1. Dual Ambient Glowing Orbs ─────────────────────────────────────
        Box(
            modifier = Modifier
                .size((380 * orbPulse).dp)
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = (-50).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GradientIndigoStart.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size((340 * orbPulse).dp)
                .align(Alignment.BottomStart)
                .offset(x = (-70).dp, y = 50.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Violet500.copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    )
                )
        )

        // ── 2. Floating Particle Accents ────────────────────────────────────
        Box(
            modifier = Modifier
                .size(10.dp)
                .align(Alignment.TopStart)
                .offset(x = 54.dp, y = (140 + floatY).dp)
                .clip(CircleShape)
                .background(GradientIndigoStart.copy(alpha = 0.45f))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-70).dp, y = (-180 - floatY).dp)
                .clip(CircleShape)
                .background(CampusTeal40.copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .align(Alignment.CenterStart)
                .offset(x = 36.dp, y = (-90 + floatY).dp)
                .clip(CircleShape)
                .background(Violet400.copy(alpha = 0.35f))
        )

        // ── 3. Center Hero: Aura + Logo + Brand Wordmark ────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Expanding Glow Aura Ring behind Logo
                Box(
                    modifier = Modifier
                        .scale(auraScale.value)
                        .alpha(auraAlpha.value * auraGlow)
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GradientIndigoStart.copy(alpha = 0.5f),
                                    Violet400.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Outer Glass Blur Ring around Badge
                Box(
                    modifier = Modifier
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .size(118.dp)
                        .clip(RoundedCornerShape(34.dp))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            ),
                            shape = RoundedCornerShape(34.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Official CampusGig Electric Indigo Lightning Badge
                    CampusGigLogoIcon(
                        size = 110.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ── Brand Wordmark ("Campus" Pure White · "Gig" Electric Indigo Gradient) ──
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color      = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    ) { append("Campus") }
                    withStyle(
                        SpanStyle(
                            brush      = Brush.horizontalGradient(
                                colors = listOf(GradientIndigoStart, GradientIndigoEnd)
                            ),
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) { append("Gig") }
                },
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffset.value.dp),
                style = TextStyle(
                    fontSize      = 38.sp,
                    letterSpacing = (-0.8).sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Tagline Badge ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .alpha(taglineAlpha.value)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                GradientIndigoStart.copy(alpha = 0.4f),
                                GradientIndigoEnd.copy(alpha = 0.2f)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "Learn  •  Earn  •  Collaborate",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize      = 13.sp,
                        letterSpacing = 0.6.sp,
                        fontWeight    = FontWeight.SemiBold
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // ── 4. Bottom Footer Version Tag ────────────────────────────────────
        Text(
            text = "CampusGig v1.0 • Student Edition",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-36).dp)
                .alpha(taglineAlpha.value),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize      = 11.sp,
                letterSpacing = 0.8.sp,
                fontWeight    = FontWeight.Medium
            ),
            color = Color.White.copy(alpha = 0.25f)
        )
    }
}
