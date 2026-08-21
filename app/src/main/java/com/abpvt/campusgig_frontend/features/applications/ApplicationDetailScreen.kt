/**
 * ApplicationDetailScreen.kt — NEW — Full application detail view
 *
 * Design spec implementation (Screen 16):
 *  - Status hero card (gradient bg based on status, centered icon + title + description)
 *  - Gig snapshot card (title, category, budget, poster) + "View Full Gig →" link
 *  - Cover letter in a styled quote block
 *  - Proposal details: 2-column grid (bid amount, applied date)
 *  - Application timeline: vertical dots with gradient connecting line
 *  - Action buttons (conditional on status): withdraw, chat, re-apply
 */
package com.abpvt.campusgig_frontend.features.applications

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ApplicationViewModelFactory
import com.abpvt.campusgig_frontend.data.model.Application
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.components.AvatarInitials
import com.abpvt.campusgig_frontend.ui.components.LeaveReviewDialog
import com.abpvt.campusgig_frontend.ui.components.getRelativeTime
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning

// ─── Status Config ─────────────────────────────────────────────────────────────
private data class StatusConfig(
    val gradientColors: List<Color>,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val accentColor: Color
)

private fun statusConfig(status: String) = when (status.lowercase()) {
    "accepted"    -> StatusConfig(
        gradientColors = listOf(Color(0xFF065F46), Color(0xFF10B981)),
        icon = Icons.Default.CheckCircle,
        title = "Application Accepted!",
        description = "The poster chose you — time to get started 🎉",
        accentColor = SemanticSuccess
    )
    "in_progress" -> StatusConfig(
        gradientColors = listOf(Color(0xFF1E3A5F), Color(0xFF1D4ED8)),
        icon = Icons.Default.Work,
        title = "Work in Progress",
        description = "You're actively working on this gig",
        accentColor = Color(0xFF3B82F6)
    )
    "completed"   -> StatusConfig(
        gradientColors = listOf(Color(0xFF134E4A), Color(0xFF0D9488)),
        icon = Icons.Default.Star,
        title = "Gig Completed!",
        description = "You delivered great work — nice job 🌟",
        accentColor = Color(0xFF14B8A6)
    )
    "rejected"    -> StatusConfig(
        gradientColors = listOf(Color(0xFF4B0000), Color(0xFF7F1D1D)),
        icon = Icons.Default.Close,
        title = "Not Selected",
        description = "The poster went with someone else this time",
        accentColor = SemanticError
    )
    "withdrawn"   -> StatusConfig(
        gradientColors = listOf(Color(0xFF1F2535), Color(0xFF353B52)),
        icon = Icons.Default.Refresh,
        title = "Application Withdrawn",
        description = "You withdrew from this gig",
        accentColor = Color(0xFF6B7280)
    )
    else -> StatusConfig( // pending
        gradientColors = listOf(Color(0xFF451A03), Color(0xFF92400E)),
        icon = Icons.Default.HourglassEmpty,
        title = "Awaiting Response",
        description = "Your application is being reviewed",
        accentColor = SemanticWarning
    )
}

@Composable
fun ApplicationDetailScreen(
    navController: NavController,
    application: Application,
    viewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).apiService
        )
    )
) {
    val config = remember(application.status) { statusConfig(application.status) }
    val relativeTime = remember(application.createdAt) { getRelativeTime(application.createdAt) }
    val poster = application.gig?.employer
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    val timelineSteps = buildList {
        add(Triple("Applied", relativeTime, true))
        add(Triple("Under Review", if (application.status == "pending") "Waiting..." else "Done", true))
        add(Triple("Decision Made", when (application.status.lowercase()) {
            "accepted", "rejected" -> "Decided"
            "pending" -> "Pending"
            else -> "Decided"
        }, application.status.lowercase() != "pending"))
        if (application.status.lowercase() in listOf("accepted", "in_progress", "completed")) {
            add(Triple("Work Started", if (application.status.lowercase() == "in_progress" || application.status.lowercase() == "completed") "In progress" else "Not yet", application.status.lowercase() in listOf("in_progress", "completed")))
        }
        if (application.status.lowercase() == "completed") {
            add(Triple("Completed", "Done", true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── STATUS HERO CARD ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.linearGradient(
                        colors = config.gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(800f, 600f)
                    )
                )
        ) {
            // Back button
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            // Hero content
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(config.icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    config.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    config.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            // Bottom fade into MaterialTheme.colorScheme.background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomStart)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background)))
            )
        }

        // ── CONTENT ────────────────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {

            // GIG SNAPSHOT CARD
            application.gig?.let { gig ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    gig.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        gig.category,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    Text(
                                        gig.formattedBudget(),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                        color = SemanticSuccess
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "View Gig →",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { navController.navigate(Routes.gigDetail(gig.id)) }
                            )
                        }

                        gig.employer?.let { employer ->
                            Spacer(Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarInitials(name = employer.name, size = 28)
                                Spacer(Modifier.width(8.dp))
                                Text("Posted by ${employer.name}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // COVER LETTER / PROPOSAL
            Text("Your Proposal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row {
                    // Left quote border accent
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(120.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        application.proposal.ifBlank { "No proposal text provided." },
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // PROPOSAL DETAILS 2-column grid
            Text("Application Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailCell("YOUR BID", "₹${application.expectedBudget.toInt()}", Modifier.weight(1f), SemanticSuccess)
                DetailCell("APPLIED", relativeTime, Modifier.weight(1f), MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(20.dp))

            // APPLICATION TIMELINE
            Text("Timeline", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp), color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))

            timelineSteps.forEachIndexed { index, (stepLabel, stepValue, isDone) ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Dot + connecting line column
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDone)
                                        Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                                    else
                                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
                                )
                        )
                        if (index < timelineSteps.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(28.dp)
                                    .background(
                                        if (isDone)
                                            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)))
                                        else
                                            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
                                    )
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.padding(bottom = if (index < timelineSteps.lastIndex) 0.dp else 4.dp)) {
                        Text(stepLabel, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal, fontSize = 14.sp), color = if (isDone) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Text(stepValue, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        Spacer(Modifier.height(14.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── ACTION BUTTONS (status-conditional) ────────────────────────────
            val poster = application.gig?.employer
            when (application.status.lowercase()) {
                "pending" -> {
                    ActionButton(
                        text = "Withdraw Application",
                        icon = Icons.Default.Close,
                        gradient = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant),
                        textColor = SemanticError,
                        onClick = { showWithdrawDialog = true }
                    )
                }
                "accepted" -> {
                    ActionButton(
                        text = "Open Chat with Poster →",
                        icon = Icons.AutoMirrored.Filled.Message,
                        gradient = listOf(GradientIndigoStart, GradientIndigoEnd),
                        textColor = Color.White,
                        onClick = {
                            if (poster != null) {
                                navController.navigate(
                                    Routes.chat(
                                        receiverId = poster.id,
                                        receiverName = poster.name,
                                        gigTitle = application.gig?.title,
                                        gigBudget = "₹${application.expectedBudget.toInt()}"
                                    )
                                )
                            }
                        }
                    )
                }
                "in_progress" -> {
                    ActionButton(
                        text = "Open Chat →",
                        icon = Icons.AutoMirrored.Filled.Message,
                        gradient = listOf(Color(0xFF1E3A5F), Color(0xFF1D4ED8)),
                        textColor = Color.White,
                        onClick = {
                            if (poster != null) {
                                navController.navigate(
                                    Routes.chat(
                                        receiverId = poster.id,
                                        receiverName = poster.name,
                                        gigTitle = application.gig?.title,
                                        gigBudget = "₹${application.expectedBudget.toInt()}"
                                    )
                                )
                            }
                        }
                    )
                }
                "completed" -> {
                    ActionButton(
                        text = "Leave a Review",
                        icon = Icons.Default.Star,
                        gradient = listOf(Color(0xFF134E4A), Color(0xFF0D9488)),
                        textColor = Color.White,
                        onClick = { showReviewDialog = true }
                    )
                }
                "rejected" -> {
                    ActionButton(
                        text = "Find Similar Gigs →",
                        icon = Icons.Default.Work,
                        gradient = listOf(GradientIndigoStart, GradientIndigoEnd),
                        textColor = Color.White,
                        onClick = { navController.navigate(Routes.GIG_LIST) }
                    )
                }
            }

            Spacer(Modifier.height(48.dp))
        }

        // ── Withdrawal Confirmation Dialog ─────────────────────────────────────
        if (showWithdrawDialog) {
            AlertDialog(
                onDismissRequest = { showWithdrawDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        "Withdraw Application?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to withdraw your application for \"${application.gig?.title ?: "this gig"}\"? This cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showWithdrawDialog = false
                            viewModel.withdrawApplication(application.id) { success ->
                                if (success) navController.popBackStack()
                            }
                        }
                    ) {
                        Text("Withdraw", color = SemanticError, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWithdrawDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }

        // ── Leave Review Dialog ───────────────────────────────────────────────
        if (showReviewDialog && poster != null) {
            LeaveReviewDialog(
                gigId = application.gig?.id ?: "",
                gigTitle = application.gig?.title ?: "Completed Gig",
                targetUserId = poster.id,
                targetUserName = poster.name,
                onDismiss = { showReviewDialog = false },
                onSubmitSuccess = {
                    showReviewDialog = false
                    navController.popBackStack()
                }
            )
        }
    }
}

// ─── Detail Cell ──────────────────────────────────────────────────────────────
@Composable
private fun DetailCell(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = MaterialTheme.colorScheme.onBackground) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.4.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 18.sp), color = valueColor)
        }
    }
}

// ─── Action Button ────────────────────────────────────────────────────────────
@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    gradient: List<Color>,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(gradient))
            .border(
                width = if (gradient.first() == MaterialTheme.colorScheme.surfaceVariant) 1.dp else 0.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(26.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = textColor, modifier = Modifier.size(18.dp))
            Text(text, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = textColor)
        }
    }
}
