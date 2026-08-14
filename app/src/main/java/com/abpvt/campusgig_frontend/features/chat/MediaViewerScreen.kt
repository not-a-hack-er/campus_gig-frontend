/**
 * MediaViewerScreen.kt — Full-screen Media Viewer
 *
 * CONCEPT: Full-screen image/file viewer. Clean, minimal — the media is the star.
 * Controls fade out automatically after 3 seconds.
 *
 * FEATURES:
 * - Black background, full-screen
 * - Auto-hiding overlays (tap to toggle)
 * - Top: back arrow + sender name + timestamp
 * - Bottom: share + download + page counter
 * - Transition: shared element expand feel
 */
package com.abpvt.campusgig_frontend.features.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.TextPrimary
import com.abpvt.campusgig_frontend.ui.theme.TextSecondary
import com.abpvt.campusgig_frontend.ui.theme.TextTertiary
import kotlinx.coroutines.delay

@Composable
fun MediaViewerScreen(
    navController: NavController,
    mediaUrl: String
) {
    var showControls by remember { mutableStateOf(true) }

    // Auto-hide controls after 3s
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        // ── Media Placeholder (gradient until real image loading is wired) ──────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GradientIndigoStart.copy(alpha = 0.15f),
                            Color.Black
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🖼️", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Image Viewer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mediaUrl.takeLast(40),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }

        // ── Top Overlay (auto-hiding) ─────────────────────────────────────────
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 48.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Shared by Rohan M.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Text(
                            "Today, 11:30 AM",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // ── Bottom Overlay (auto-hiding) ──────────────────────────────────────
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { /* share */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    // Counter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("1 of 3", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }

                    // Download button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(colors = listOf(GradientIndigoStart, GradientIndigoEnd))
                            )
                            .clickable { /* download */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
