/**
 * ChatListScreen.kt — v2.0
 *
 * CONCEPT: Premium messaging list. Feels like the best of iMessage and Telegram.
 * Active conversations should feel like living connections, not a data list.
 *
 * FEATURES:
 * - "Active Now" horizontal scroll row with online dots
 * - Conversation rows: 72dp, unread bold style, left gradient border
 * - Unread badge, timestamp, gig context chip
 * - Empty state with Browse Gigs CTA
 */
package com.abpvt.campusgig_frontend.features.chat

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ChatViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.data.model.ConversationItem
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.components.AvatarInitials
import androidx.compose.material3.Icon
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess

@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).chatRepository
        )
    )
) {
    val conversationsState by viewModel.conversations.collectAsState()
    val onlineUserIds by viewModel.onlineUserIds.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                    text = "Messages",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .clickable { /* compose new */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "New chat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Sticky Search Bar ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        decorationBox = { inner ->
                            Box {
                                if (searchQuery.isEmpty()) Text("Search conversations...", style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)))
                                inner()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = conversationsState) {
                is Resource.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }

                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚠️", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Couldn't load messages", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Retry", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { viewModel.loadConversations() }, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                is Resource.Success<List<ConversationItem>> -> {
                    val allConversations = state.data
                    val conversations = if (searchQuery.isBlank()) allConversations
                    else allConversations.filter { it.user.name.contains(searchQuery, ignoreCase = true) }

                    if (conversations.isEmpty() && allConversations.isEmpty()) {
                        // Empty state
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                                Text("💬", fontSize = 56.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No conversations yet", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Browse gigs and apply to start chatting with posters", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(20.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                                        .clickable { navController.navigate(Routes.GIG_LIST) }
                                        .padding(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text("Browse Gigs", style = MaterialTheme.typography.labelLarge, color = Color.White)
                                }
                            }
                        }
                    } else {
                        // "Active Now" row — filter conversations whose peer is currently online
                        val activeUsers = conversations.filter { onlineUserIds.contains(it.user.id) }
                        if (activeUsers.isNotEmpty()) {
                            Column {
                                Text(
                                    "Active Now",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(activeUsers) { item ->
                                        val user = item.user
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable { navController.navigate(Routes.chat(user.id, user.name)) }
                                        ) {
                                            Box {
                                                AvatarInitials(name = user.name, size = 52)
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(SemanticSuccess)
                                                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                                        .align(Alignment.BottomEnd)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = user.name.split(" ").firstOrNull() ?: user.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        // Conversation list
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(conversations, key = { it.conversationId }) { item ->
                                val isOnline = onlineUserIds.contains(item.user.id)
                                ConversationRow(
                                    item = item,
                                    isOnline = isOnline,
                                    onClick = { navController.navigate(Routes.chat(item.user.id, item.user.name)) }
                                )
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
private fun ConversationRow(item: ConversationItem, isOnline: Boolean, onClick: () -> Unit) {
    val user = item.user
    val timeLabel = formatRelativeTime(item.updatedAt)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online dot
            Box {
                AvatarInitials(name = user.name, size = 48)
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(SemanticSuccess)
                            .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.lastMessage.ifBlank { "Tap to start chatting" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Gig context chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Re: ${user.college.ifBlank { "Campus Gig" }}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(timeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
    }
}

/**
 * Format timestamp to a relative readable string (e.g. 5m ago, 2h ago)
 */
fun formatRelativeTime(isoString: String): String {
    if (isoString.isBlank()) return ""
    return try {
        val instant = java.time.Instant.parse(isoString)
        val now = java.time.Instant.now()
        val diffSeconds = java.time.Duration.between(instant, now).seconds
        when {
            diffSeconds < 60 -> "Just now"
            diffSeconds < 3600 -> "${diffSeconds / 60}m ago"
            diffSeconds < 86400 -> "${diffSeconds / 3600}h ago"
            else -> "${diffSeconds / 86400}d ago"
        }
    } catch (e: Exception) {
        ""
    }
}
