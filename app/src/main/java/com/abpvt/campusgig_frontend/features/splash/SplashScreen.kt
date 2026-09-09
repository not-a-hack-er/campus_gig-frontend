/**
 * SplashScreen.kt — Premium, high-quality launch experience for Campus Vault.
 *
 * 5-Phase Motion Sequence:
 * 1. Phase 1 (Preparation): Logo starts in subtle initial state (scale 0.95f, alpha 0f).
 * 2. Phase 2 (Reveal): Ultra-smooth CubicBezier fade-in (500ms).
 * 3. Phase 3 (Gentle Scale): Micro-scaling up to 1.0f without transform blur (400ms).
 * 4. Phase 4 (Settle): Clean 300ms hold on final resting state.
 * 5. Phase 5 (Transition): Smooth cross-fade exit (350ms) into the application.
 */
package com.abpvt.campusgig_frontend.features.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.logo.CampusGigFullLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EaseOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1.0f)
private val EaseInOutCubic = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1.0f)

@Composable
fun SplashScreen(
    navController: NavController,
    isLoggedIn: Boolean
) {
    // ── 5-Phase Motion States ───────────────────────────────────────────────
    val logoScale   = remember { Animatable(0.95f) }
    val logoAlpha   = remember { Animatable(0f) }
    val textAlpha   = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }

    // ── Orchestrated Sequence ────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        // Phase 1 -> 2: Reveal Fade-in (500ms)
        launch {
            logoAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 500, easing = EaseOutCubic)
            )
        }

        // Phase 3: Micro-scale to 1.0f (400ms staggered)
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 650, easing = EaseOutCubic)
            )
        }

        // Sub-text subtle reveal
        launch {
            textAlpha.animateTo(
                targetValue = 0.75f,
                animationSpec = tween(durationMillis = 500, delayMillis = 250, easing = EaseOutCubic)
            )
        }

        // Phase 4: Settle & Hold (300ms)
        delay(950)

        // Phase 5: Smooth Transition Exit (350ms)
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 350, easing = LinearEasing)
        )

        // Navigate to Home or Login
        val destination = if (isLoggedIn) Routes.HOME else Routes.LOGIN
        navController.navigate(destination) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    // ── Layout Canvas ────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(Color(0xFF0B0F19)),
        contentAlignment = Alignment.Center
    ) {
        // Center Focal Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CampusGigFullLogo(
                width = 284.dp,
                onDarkBackground = true,
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )
        }

        // Bottom Clean Version Tag
        Text(
            text = "Campus Vault  •  v1.0",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-36).dp)
                .alpha(textAlpha.value),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize      = 12.sp,
                letterSpacing = 1.0.sp,
                fontWeight    = FontWeight.Medium
            ),
            color = Color.White.copy(alpha = 0.4f)
        )
    }
}
