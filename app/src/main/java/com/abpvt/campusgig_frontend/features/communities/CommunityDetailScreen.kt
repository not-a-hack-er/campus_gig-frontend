/**
 * CommunityDetailScreen.kt — v2.0 — Community Home Page
 *
 * CONCEPT: Like a private campus club. The banner is identity, feed is heartbeat.
 * CollapsingToolbar behavior via LazyColumn + stickyHeader.
 *
 * LAYOUT:
 * - Hero banner 220dp (collapses on scroll) with avatar + Join/Leave button
 * - Metadata: name, category, description, stats row, member preview
 * - Sticky tab bar: Feed | Members | About
 * - Feed: post cards with like/comment/share
 * - Members: list rows with role badges
 * - About: full description, rules, creator info
 * - FAB: gradient circle "Create Post"
 */
package com.abpvt.campusgig_frontend.features.communities

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.CommunityViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Community
import com.abpvt.campusgig_frontend.data.model.Post
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.GradientTealEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientTealStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun communityGradient(name: String): List<Color> {
    val gradients = listOf(
        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
        listOf(Color(0xFF14B8A6), Color(0xFF06B6D4)),
        listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
        listOf(Color(0xFF10B981), Color(0xFF3B82F6)),
        listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
        listOf(Color(0xFF6366F1), Color(0xFF14B8A6)),
    )
    return gradients[(name.hashCode() and 0x7FFFFFFF) % gradients.size]
}

val detailTabs = listOf("Feed", "Members", "About")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommunityDetailScreen(
    navController: NavController,
    communityId: String,
    viewModel: CommunityViewModel = viewModel(
        factory = CommunityViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).communityRepository
        )
    )
) {
    LaunchedEffect(communityId) {
        viewModel.loadCommunityById(communityId)
        viewModel.loadFeed(communityId)
    }

    val communityState by viewModel.selectedCommunity.collectAsState()
    val feedState by viewModel.feed.collectAsState()
    val isMember by viewModel.isMember.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val state = communityState) {
            null, is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is Resource.Success<Community> -> {
                val community = state.data
                val gradColors = communityGradient(community.name)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {

                    // ── Hero Banner ───────────────────────────────────────────
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                            // Banner gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(Brush.linearGradient(gradColors))
                            ) {
                                // Dark overlay for readability
                                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(0.3f), Color.Transparent, Color.Black.copy(0.4f)))))
                            }

                            // Back button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .clickable { navController.popBackStack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            // More button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            // Community avatar (bottom-left, overlapping)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = 20.dp, y = 32.dp)
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Brush.linearGradient(gradColors)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(community.name.first().uppercaseChar().toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                                }
                            }

                            // Join / Leave button (bottom-right)
                            Box(
                                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 8.dp)
                            ) {
                                if (isMember) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .border(1.dp, Color.White.copy(0.5f), RoundedCornerShape(20.dp))
                                            .background(Color.Black.copy(0.3f))
                                            .clickable { viewModel.leaveCommunity(community.id) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text("✓ Member", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White)
                                            .clickable { viewModel.joinCommunity(community.id) }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text("Join Community", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = GradientIndigoStart)
                                    }
                                }
                            }
                        }
                    }

                    // ── Metadata section ──────────────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(community.name, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.primary.copy(0.12f)).border(1.dp, MaterialTheme.colorScheme.primary.copy(0.3f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text(community.category.ifBlank { "General" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                if (community.isPrivate) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                        Text("🔒 Private", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(community.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Stats row
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                CommunityStat("${community.memberCount}", "Members")
                                CommunityStat("89", "Posts")
                                CommunityStat("12", "Active Now")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Overlapping member avatars
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.height(28.dp)) {
                                    listOf("A", "S", "D", "N", "K").forEachIndexed { i, initial ->
                                        Box(
                                            modifier = Modifier
                                                .offset(x = (i * 18).dp)
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(gradColors))
                                                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(initial, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(96.dp))
                                Text("and ${community.memberCount - 5} others", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        }
                    }

                    // ── Sticky Tab Bar ────────────────────────────────────────
                    stickyHeader {
                        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                detailTabs.forEachIndexed { i, tab ->
                                    val isActive = selectedTab == i
                                    Column(modifier = Modifier.clickable { selectedTab = i }, horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(tab, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal), color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(modifier = Modifier.height(2.dp).width(if (isActive) 24.dp else 0.dp).clip(RoundedCornerShape(1.dp)).background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))))
                                    }
                                }
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(MaterialTheme.colorScheme.outline))
                        }
                    }

                    // ── Tab Content ───────────────────────────────────────────
                    when (selectedTab) {
                        0 -> { // FEED
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            when (val state = feedState) {
                                is Resource.Loading -> item {
                                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                    }
                                }
                                is Resource.Error -> item {
                                    Text("Could not load posts. Tap Feed to retry.", modifier = Modifier.padding(20.dp), color = SemanticSuccess)
                                }
                                is Resource.Success -> {
                                    if (state.data.isEmpty()) item {
                                        Text("No posts yet. Start the conversation.", modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    items(state.data, key = { it.id }) { post ->
                                        PostCard(post = post, modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp))
                                    }
                                }
                            }
                        }
                        1 -> { // MEMBERS
                            item { Spacer(modifier = Modifier.height(12.dp)) }
                            items(community.members) { member ->
                                MemberRow(name = member.name, college = member.college, role = if (member.id == community.creator?.id) "Admin" else "Member", modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                            }
                            if (community.members.isEmpty()) {
                                item {
                                    // Show creator as fallback
                                    community.creator?.let { creator ->
                                        MemberRow(name = creator.name, college = creator.college, role = "Admin", modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                                    }
                                }
                            }
                        }
                        2 -> { // ABOUT
                            item {
                                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                                    Text("About", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(community.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Community Rules", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    listOf("Be respectful and professional", "No spam or self-promotion without permission", "Keep discussions relevant to the community topic", "Help each other — this is a learning space").forEachIndexed { i, rule ->
                                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text("${i + 1}.", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(20.dp))
                                            Text(rule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    community.creator?.let { creator ->
                                        Text("Created by", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Brush.linearGradient(gradColors)), contentAlignment = Alignment.Center) {
                                                Text(creator.name.first().uppercaseChar().toString(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(creator.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                                                Text(creator.college, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                            }
                                        }
                                    }
                                    Text("Created ${community.createdAt.take(10)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }

                // ── FAB: Create Post ──────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 20.dp, bottom = 20.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                            .clickable { navController.navigate(Routes.createPost(communityId)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Create, contentDescription = "Create Post", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun PostCard(post: Post, modifier: Modifier = Modifier) {
    val authorName = post.author?.name?.ifBlank { "Campus member" } ?: "Campus member"
    val postedAt = runCatching {
        DateTimeFormatter.ofPattern("d MMM, h:mm a").format(Instant.parse(post.createdAt).atZone(ZoneId.systemDefault()))
    }.getOrDefault("Recently")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            // Author row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(GradientIndigoStart.copy(0.2f)), contentAlignment = Alignment.Center) {
                    Text(authorName.first().uppercaseChar().toString(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(authorName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                    Text(postedAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Post text
            Text(post.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 5)
            Spacer(modifier = Modifier.height(12.dp))
            // Engagement row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ThumbUp, contentDescription = "Likes", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.likesCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
private fun MemberRow(name: String, college: String, role: String, modifier: Modifier = Modifier) {
    val roleColor = when (role) { "Admin" -> MaterialTheme.colorScheme.primary; "Moderator" -> GradientTealEnd; else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(GradientIndigoStart.copy(0.15f)), contentAlignment = Alignment.Center) {
            Text(name.first().uppercaseChar().toString(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
            Text(college.ifBlank { "Student" }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(roleColor.copy(0.1f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(role, style = MaterialTheme.typography.labelSmall, color = roleColor)
        }
    }
}
