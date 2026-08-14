/**
 * PostDetailScreen.kt — Full post with threaded comments
 *
 * CONCEPT: Full post + threaded comments. Conversational like Discord threads but clean.
 *
 * FEATURES:
 * - Full post (author, content, engagement row)
 * - Comments section: sort by Top/Recent
 * - Comment rows: avatar + name + text + timestamp + heart icon + Reply
 * - Indented replies with connecting line
 * - Sticky comment input at bottom
 */
package com.abpvt.campusgig_frontend.features.communities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.abpvt.campusgig_frontend.ui.theme.TextPrimary
import com.abpvt.campusgig_frontend.ui.theme.TextSecondary
import com.abpvt.campusgig_frontend.ui.theme.TextTertiary

private data class Comment(val id: String, val author: String, val text: String, val time: String, var likes: Int = 0, val replies: List<Comment> = emptyList())

private val mockComments = listOf(
    Comment("c1", "Rohan Mehta", "This is so true! I've been using Compose Multiplatform for a few weeks and the iOS support is surprisingly solid.", "1h ago", 12, listOf(
        Comment("c1r1", "Akarsh Bajpai", "Agreed, but the memory management is still something to watch.", "45m ago", 5),
        Comment("c1r2", "Sarah Jenkins", "The performance on older devices is where I see the most issues.", "30m ago", 3)
    )),
    Comment("c2", "Priya Rao", "Thanks for sharing this! Super helpful for our upcoming project. Which libraries are you using for navigation?", "2h ago", 8),
    Comment("c3", "Vikram Nair", "Been wanting to try this for a while. Did you face any issues with the Compose preview on multiplatform?", "3h ago", 6),
    Comment("c4", "Neha Sharma", "Great post! Would love to see a follow-up on performance benchmarks 🙏", "4h ago", 15),
)

@Composable
fun PostDetailScreen(
    navController: NavController,
    postId: String
) {
    var commentText by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf("Top") }
    val comments = remember { mutableStateListOf(*mockComments.toTypedArray()) }

    Box(
        modifier = Modifier.fillMaxSize().background(Surface1).imePadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Surface2).clickable { navController.popBackStack() }, contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Post", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    }
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }

            // ── Full Post ────────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Surface2)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))), contentAlignment = Alignment.Center) {
                                Text("A", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Akarsh Bajpai", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                                Text("2h ago · Android Devs", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Just shipped the new animation system for CampusGig! Compose animations are incredible 🚀\n\nReally enjoying the spring physics API — makes UI feel alive without effort. The AnimatedContent composable especially is a game changer for state transitions. Would love to know if anyone else is using spring-based animations in production.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "Like", tint = Indigo400, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("24", style = MaterialTheme.typography.labelSmall, color = Indigo400)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💬", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${comments.size} Comments", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = TextTertiary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Comments header ──────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${comments.size} Comments", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Top", "Recent").forEach { sort ->
                            Text(
                                sort,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (sortMode == sort) FontWeight.SemiBold else FontWeight.Normal),
                                color = if (sortMode == sort) Indigo400 else TextTertiary,
                                modifier = Modifier.clickable { sortMode = sort }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Comment rows ─────────────────────────────────────────────────
            items(comments) { comment ->
                CommentRow(comment = comment, onReply = {}, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                // Indented replies
                comment.replies.forEach { reply ->
                    Row(modifier = Modifier.padding(start = 40.dp, end = 16.dp, bottom = 4.dp)) {
                        // Connecting line
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderSubtle))
                        Spacer(modifier = Modifier.width(12.dp))
                        CommentRow(comment = reply, isReply = true, onReply = {})
                    }
                }
            }
        }

        // ── Sticky Comment Input ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Surface2)
                .border(1.dp, BorderSubtle, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))), contentAlignment = Alignment.Center) {
                    Text("A", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Surface3)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (commentText.isEmpty()) {
                        Text("Add a comment...", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                    BasicTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(Indigo400),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (commentText.isNotBlank()) Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)) else Brush.linearGradient(listOf(Surface3, Surface3)))
                        .clickable(enabled = commentText.isNotBlank()) {
                            // In production: submit comment via VM
                            commentText = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = if (commentText.isNotBlank()) Color.White else TextTertiary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CommentRow(comment: Comment, isReply: Boolean = false, onReply: () -> Unit, modifier: Modifier = Modifier) {
    var liked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(comment.likes) }

    Row(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(if (isReply) 28.dp else 34.dp)
                .clip(CircleShape)
                .background(GradientIndigoStart.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(comment.author.first().uppercaseChar().toString(), fontSize = if (isReply) 11.sp else 13.sp, color = Indigo400, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(comment.author, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = if (isReply) 11.sp else 13.sp), color = TextPrimary)
                Text(comment.time, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(comment.text, style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isReply) 12.sp else 13.sp), color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { if (!liked) likeCount++ else likeCount--; liked = !liked }
                ) {
                    Icon(if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Like", tint = if (liked) Indigo400 else TextTertiary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("$likeCount", style = MaterialTheme.typography.labelSmall, color = if (liked) Indigo400 else TextTertiary)
                }
                if (!isReply) {
                    Text("Reply", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = Indigo400, modifier = Modifier.clickable { onReply() })
                }
            }
        }
    }
}
