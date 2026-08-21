/**
 * SplashScreen.kt — Premium animated launch experience for CampusVault.
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.BackgroundDark
import com.abpvt.campusgig_frontend.ui.theme.BackgroundBase
import com.abpvt.campusgig_frontend.ui.theme.CampusIndigo40
import com.abpvt.campusgig_frontend.ui.theme.CampusIndigo80
import com.abpvt.campusgig_frontend.ui.theme.CampusTeal40
import com.abpvt.campusgig_frontend.ui.theme.CampusTeal80
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.Violet500
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
            // ── V-Lock Logo Badge ─────────────────────────────────────────
            // Brand mark: geometric V with keyhole negative space at base vertex.
            // Built entirely in Compose Canvas — no external asset needed.
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .size(104.dp)
                    // Deep navy brand background
                    .background(
                        color = BackgroundBase,
                        shape = RoundedCornerShape(26.dp)
                    )
                    // Subtle glass border
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subtle indigo ambient glow behind the mark
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GradientIndigoStart.copy(alpha = 0.28f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // The V-Lock mark drawn on Canvas
                Canvas(modifier = Modifier.size(56.dp)) {
                    val w = size.width
                    val h = size.height

                    // ── Gradient brush for the V arms ────────────────────
                    val vGradient = Brush.linearGradient(
                        colors = listOf(GradientIndigoStart, GradientIndigoMid, Violet500),
                        start = Offset(0f, 0f),
                        end   = Offset(w, h)
                    )

                    // ── V shape as a filled Path ─────────────────────────
                    // The V occupies the full canvas width with a vertex at
                    // ~80% of the height to leave room for the keyhole below.
                    val armThickness = w * 0.22f
                    val vertexY     = h * 0.80f   // where the two arms meet
                    val topInset    = 0f           // arms start at canvas top

                    val vPath = Path().apply {
                        // Left arm — outer edge (top-left to vertex)
                        moveTo(0f, topInset)
                        lineTo(armThickness, topInset)
                        // Left arm — inner edge meets at vertex center-bottom
                        lineTo(w * 0.5f, vertexY)
                        // Right arm — inner edge from vertex
                        lineTo(w - armThickness, topInset)
                        lineTo(w, topInset)
                        // Right arm — outer edge back to vertex
                        lineTo(w * 0.5f, vertexY + armThickness * 0.5f)
                        close()
                    }

                    // ── Keyhole as a subtractive path ────────────────────
                    // Circle + rectangular slot centered at the V vertex
                    val khRadius = w * 0.085f
                    val khCx     = w * 0.5f
                    val khCy     = vertexY - khRadius * 0.4f
                    val slotW    = khRadius * 0.7f
                    val slotH    = khRadius * 1.6f

                    val keyholePath = Path().apply {
                        // Circle
                        addOval(
                            Rect(
                                center = Offset(khCx, khCy),
                                radius = khRadius
                            )
                        )
                        // Rectangular slot below the circle
                        addRect(
                            Rect(
                                offset = Offset(khCx - slotW / 2f, khCy + khRadius * 0.55f),
                                size   = Size(slotW, slotH)
                            )
                        )
                    }

                    // ── Final mark = V minus keyhole ──────────────────────
                    val markPath = Path().apply {
                        op(vPath, keyholePath, PathOperation.Difference)
                    }

                    drawPath(
                        path  = markPath,
                        brush = vGradient
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand wordmark: "Campus" white · "Vault" indigo gradient
            // Typography: heavy weight, tight tracking (-0.5sp) — brand spec
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
                                colors = listOf(GradientIndigoStart, Violet500)
                            ),
                            fontWeight = FontWeight.ExtraBold
                        )
                    ) { append("Vault") }
                },
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffset.value.dp),
                style = TextStyle(
                    fontSize      = 34.sp,
                    letterSpacing = (-0.5).sp
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
