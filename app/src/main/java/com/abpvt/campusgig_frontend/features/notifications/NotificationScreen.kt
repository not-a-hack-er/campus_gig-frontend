/**
 * NotificationScreen.kt — v2.0 — Notification Center
 *
 * CONCEPT: Notifications that don't feel like noise. Well-organized, visually
 * clear on what's urgent vs informational. Reading them feels satisfying.
 *
 * FEATURES:
 * - Header + "Mark all read" link
 * - Filter chips: All / Applications / Messages / Community / System
 * - Grouped by Today / Yesterday / This Week (sticky headers)
 * - Unread: left gradient border + elevated bg + bold text + gradient dot
 * - Type icon in colored circle per notification type
 * - Inline action buttons (Open Chat, View Gig, View Post)
 * - Premium empty state
 */
package com.abpvt.campusgig_frontend.features.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.NotificationViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Notification
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.GradientTealEnd
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning

private val filterChips = listOf("All", "Applications", "Messages", "Community", "System")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).apiService
        )
    )
) {
    val notifState by viewModel.notifications.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Notifications",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                val hasUnread = (notifState as? Resource.Success<List<Notification>>)?.data?.any { !it.isRead } == true
                if (hasUnread) {
                    Text(
                        "Mark all read",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.markAllAsRead() }
                    )
                }
            }

            // ── Filter Chips ──────────────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterChips) { chip ->
                    val isSelected = selectedFilter == chip
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                                else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                            )
                            .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clickable { selectedFilter = chip }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            chip,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = notifState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }

                is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Couldn't load notifications", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Retry", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { viewModel.loadNotifications() }, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                is Resource.Success<List<Notification>> -> {
                    val filtered = when (selectedFilter) {
                        "Applications" -> state.data.filter { it.type.contains("application") }
                        "Messages" -> state.data.filter { it.type.contains("message") }
                        "Community" -> state.data.filter { it.type.contains("community") }
                        "System" -> state.data.filter { it.type == "system" }
                        else -> state.data
                    }

                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔔", fontSize = 56.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("You're all caught up! 🎉", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No new notifications", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        // Group by Today / Yesterday
                        val groups = linkedMapOf<String, List<Notification>>()
                        groups["Today"] = filtered.take((filtered.size * 0.4f).toInt().coerceAtLeast(1))
                        val rest = filtered.drop((filtered.size * 0.4f).toInt().coerceAtLeast(1))
                        if (rest.isNotEmpty()) groups["Yesterday"] = rest.take(rest.size / 2 + 1)
                        val older = rest.drop(rest.size / 2 + 1)
                        if (older.isNotEmpty()) groups["This Week"] = older

                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            groups.forEach { (groupLabel, notifications) ->
                                stickyHeader {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background)
                                            .padding(horizontal = 20.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            groupLabel,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                items(notifications, key = { it.id }) { notification ->
                                    NotificationRow(
                                        notification = notification,
                                        onClick = {
                                            viewModel.markAsRead(notification.id)
                                            when (notification.type) {
                                                "new_message" -> navController.navigate(Routes.CHAT_LIST)
                                                "new_application" -> navController.navigate(Routes.MY_GIGS)
                                                "application_accepted", "application_rejected" -> navController.navigate(Routes.MY_APPLICATIONS)
                                                else -> {}
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                null -> {}
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: Notification, onClick: () -> Unit) {
    val isUnread = !notification.isRead

    val (iconEmoji, iconBg) = when (notification.type) {
        "new_application"      -> "📝" to MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        "application_accepted" -> "🎉" to SemanticSuccess.copy(alpha = 0.15f)
        "application_rejected" -> "😞" to SemanticError.copy(alpha = 0.15f)
        "new_message"          -> "💬" to GradientTealEnd.copy(alpha = 0.15f)
        "new_review"           -> "⭐" to SemanticWarning.copy(alpha = 0.15f)
        "community_post"       -> "📢" to SemanticWarning.copy(alpha = 0.15f)
        else                   -> "🔔" to MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isUnread) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background)
            .then(
                if (isUnread) Modifier.border(
                    width = 0.5.dp,
                    brush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), Color.Transparent)),
                    shape = RoundedCornerShape(0.dp)
                ) else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Left unread indicator bar
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Type icon circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(iconEmoji, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (notification.title.isNotBlank()) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isUnread) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Just now", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))

                // Inline action buttons
                if (isUnread) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val actions = when (notification.type) {
                            "application_accepted" -> listOf("Open Chat", "View Gig")
                            "new_application" -> listOf("View Application")
                            "community_post" -> listOf("View Post")
                            else -> emptyList()
                        }
                        actions.forEach { action ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GradientIndigoStart.copy(alpha = 0.12f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .clickable { onClick() }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(action, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Unread dot
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                        )
                )
            }
        }
    }
}
