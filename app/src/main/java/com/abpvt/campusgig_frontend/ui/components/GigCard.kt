/**
 * GigCard.kt — Reusable card composable for displaying a single gig.
 *
 * Redesigned for full light/dark mode support using MaterialTheme.colorScheme.
 * In light mode: crisp white card on soft lavender background.
 * In dark mode: original dark Surface card.
 */
package com.abpvt.campusgig_frontend.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abpvt.campusgig_frontend.data.model.Gig
import com.abpvt.campusgig_frontend.ui.theme.Emerald500
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.StatusAccepted
import com.abpvt.campusgig_frontend.ui.theme.StatusInProgress
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.absoluteValue

@Composable
fun GigCard(
    gig: Gig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSaved by remember { mutableStateOf(false) }

    val matchPercentage = remember(gig.id) {
        val hash = gig.title.hashCode().absoluteValue
        85 + (hash % 14)
    }

    val relativeTime = remember(gig.createdAt) {
        getRelativeTime(gig.createdAt)
    }

    val employerRating = gig.employer?.rating ?: 0.0
    val employerReviewCount = gig.employer?.reviewCount ?: 0

    val cardBg    = MaterialTheme.colorScheme.surface
    val textMain  = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val borderCol = MaterialTheme.colorScheme.outline
    val primary   = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderCol),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // ── Header: Client info + Match Badge + Bookmark ──────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarInitials(
                    name = gig.employer?.name ?: "Unknown",
                    size = 36
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = gig.employer?.name ?: "Unknown",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = textMain,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        // Verified dot
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Emerald500.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, Emerald500, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", fontSize = 7.sp, color = Emerald500, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Rating / Client status
                    if (employerRating > 0.0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "%.1f (%d)".format(employerRating, employerReviewCount),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFFF59E0B)
                            )
                        }
                    } else {
                        Text(
                            text = "New Client",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = textMuted
                        )
                    }
                }

                // Match Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, primary.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = "$matchPercentage% Match",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = primary
                    )
                }

                // Bookmark
                IconButton(
                    onClick = { isSaved = !isSaved },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Gig",
                        tint = if (isSaved) primary else textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Gig Title ─────────────────────────────────────────────────
            Text(
                text = gig.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    lineHeight = 23.sp
                ),
                fontWeight = FontWeight.Bold,
                color = textMain,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Budget + Category Tag ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Budget — Emerald green gradient
                Text(
                    text = "₹${gig.budget.toInt()}",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF059669), Color(0xFF10B981))
                        ),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                CategoryChip(label = gig.category)
            }

            // ── Skills ────────────────────────────────────────────────────
            if (gig.skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    gig.skills.take(3).forEach { skill -> SkillChip(label = skill) }
                    if (gig.skills.size > 3) SkillChip(label = "+${gig.skills.size - 3}")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(borderCol)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Footer: Applicants + Time ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Applicants",
                        tint = textMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    val safeCount = kotlin.math.max(0, gig.applicationsCount)
                    Text(
                        text = "$safeCount applicant${if (safeCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = textMuted
                    )
                }
                Text(
                    text = relativeTime,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = textMuted
                )
            }
        }
    }
}

// ─── CategoryChip ─────────────────────────────────────────────────────────────
@Composable
fun CategoryChip(label: String, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = primary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, primary.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            ),
            color = primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ─── SkillChip ────────────────────────────────────────────────────────────────
@Composable
fun SkillChip(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─── StatusBadge ──────────────────────────────────────────────────────────────
@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "open"        -> Triple(StatusAccepted.copy(alpha = 0.10f),    StatusAccepted,   "Open")
        "closed"      -> Triple(StatusClosed.copy(alpha = 0.10f),      StatusClosed,     "Closed")
        "in_progress" -> Triple(StatusInProgress.copy(alpha = 0.10f),  StatusInProgress, "In Progress")
        "pending"     -> Triple(StatusPending.copy(alpha = 0.10f),     StatusPending,    "Pending")
        "accepted"    -> Triple(StatusAccepted.copy(alpha = 0.10f),    StatusAccepted,   "Accepted")
        "rejected"    -> Triple(StatusClosed.copy(alpha = 0.10f),      StatusClosed,     "Rejected")
        "completed"   -> Triple(StatusCompleted.copy(alpha = 0.10f),   StatusCompleted,  "Completed")
        else          -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, status)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).background(textColor, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

// ─── AvatarInitials ───────────────────────────────────────────────────────────
@Composable
fun AvatarInitials(
    name: String,
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    val avatarColors = listOf(
        Color(0xFF4F46E5), Color(0xFF059669), Color(0xFF9333EA),
        Color(0xFFE23744), Color(0xFF0284C7), Color(0xFFEA580C)
    )
    val avatarColor = avatarColors[(name.firstOrNull()?.code ?: 0) % avatarColors.size]

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(avatarColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = (size * 0.4).sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
    }
}

// ─── Date Parser ───────────────────────────────────────────────────────────────
fun getRelativeTime(isoString: String): String {
    if (isoString.isBlank()) return "Recently"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = sdf.parse(isoString) ?: return "Recently"
        val diff = System.currentTimeMillis() - date.time
        val seconds = diff / 1000; val minutes = seconds / 60
        val hours = minutes / 60;  val days = hours / 24
        when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours   < 24 -> "${hours}h ago"
            else         -> "${days}d ago"
        }
    } catch (e: Exception) {
        try {
            val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf2.parse(isoString) ?: return "Recently"
            val diff = System.currentTimeMillis() - date.time
            val seconds = diff / 1000; val minutes = seconds / 60
            val hours = minutes / 60;  val days = hours / 24
            when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                hours   < 24 -> "${hours}h ago"
                else         -> "${days}d ago"
            }
        } catch (ex: Exception) { "Recently" }
    }
}

private val StatusPending    get() = com.abpvt.campusgig_frontend.ui.theme.StatusPending
private val StatusAccepted   get() = com.abpvt.campusgig_frontend.ui.theme.StatusAccepted
private val StatusClosed     get() = com.abpvt.campusgig_frontend.ui.theme.StatusRejected
private val StatusInProgress get() = com.abpvt.campusgig_frontend.ui.theme.StatusInProgress
private val StatusCompleted  get() = com.abpvt.campusgig_frontend.ui.theme.StatusCompleted
