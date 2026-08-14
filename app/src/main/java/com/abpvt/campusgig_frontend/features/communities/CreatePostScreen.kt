/**
 * CreatePostScreen.kt — Distraction-free writing experience
 *
 * CONCEPT: Like drafting in Notion. The post area is the hero.
 * Auto-focus on entry. Bottom toolbar for rich text actions.
 *
 * FEATURES:
 * - Community badge chip (which community you're posting to)
 * - Full-width text area, transparent bg, placeholder
 * - Media preview (thumbnail + remove button)
 * - Bottom toolbar: Image | Link | Code | Bold | Italic + char counter
 * - Gradient "Post" button (disabled until text entered)
 * - Code mode: monospace font, dark tint
 */
package com.abpvt.campusgig_frontend.features.communities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.ui.theme.BorderSubtle
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.Indigo400
import com.abpvt.campusgig_frontend.ui.theme.Surface1
import com.abpvt.campusgig_frontend.ui.theme.Surface2
import com.abpvt.campusgig_frontend.ui.theme.Surface3
import com.abpvt.campusgig_frontend.ui.theme.Surface4
import com.abpvt.campusgig_frontend.ui.theme.TextPrimary
import com.abpvt.campusgig_frontend.ui.theme.TextSecondary
import com.abpvt.campusgig_frontend.ui.theme.TextTertiary

@Composable
fun CreatePostScreen(
    navController: NavController,
    communityId: String
) {
    var postText by remember { mutableStateOf("") }
    var isCodeMode by remember { mutableStateOf(false) }
    var hasImage by remember { mutableStateOf(false) }

    val isPostEnabled = postText.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface1)
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface1)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "× Cancel",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isPostEnabled)
                                Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                            else
                                Brush.linearGradient(listOf(Surface3, Surface3))
                        )
                        .clickable(enabled = isPostEnabled) { navController.popBackStack() }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Post",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isPostEnabled) Color.White else TextTertiary
                    )
                }
            }

            // ── Community badge ────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("C", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(GradientIndigoStart.copy(alpha = 0.08f))
                        .border(1.dp, GradientIndigoStart.copy(0.3f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Posting to community",
                        style = MaterialTheme.typography.labelSmall,
                        color = Indigo400
                    )
                }
            }

            // ── Author row ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Akarsh Bajpai", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Text("Sharing with community", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
            }

            // ── Text area ──────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                if (postText.isEmpty()) {
                    Text(
                        "What's on your mind? Share with the community...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextTertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                BasicTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontFamily = if (isCodeMode) FontFamily.Monospace else FontFamily.Default,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(Indigo400),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isCodeMode) Modifier.clip(RoundedCornerShape(8.dp)).background(Surface2) else Modifier)
                                .padding(if (isCodeMode) 12.dp else 0.dp)
                        ) { innerTextField() }
                    }
                )

                // Image preview
                if (hasImage) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Surface3)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(GradientIndigoStart.copy(0.3f), GradientIndigoEnd.copy(0.2f)))))
                        Text("📷 Image Preview", modifier = Modifier.align(Alignment.Center), color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(0.5f))
                                .clickable { hasImage = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove image", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // ── Bottom toolbar ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Image, contentDescription = "Image", tint = TextTertiary, modifier = Modifier.size(22.dp).clickable { hasImage = true })
                    Icon(Icons.Default.Link, contentDescription = "Link", tint = TextTertiary, modifier = Modifier.size(22.dp))
                    Icon(Icons.Default.Code, contentDescription = "Code", tint = if (isCodeMode) Indigo400 else TextTertiary, modifier = Modifier.size(22.dp).clickable { isCodeMode = !isCodeMode })
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = TextTertiary, modifier = Modifier.size(22.dp))
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = TextTertiary, modifier = Modifier.size(22.dp))
                }
                Text("${postText.length} chars", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
    }
}
