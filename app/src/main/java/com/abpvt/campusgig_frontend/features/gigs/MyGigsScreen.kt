/**
 * MyGigsScreen.kt — v3.0 — Employer Gig Management Dashboard
 *
 * Architecture:
 *  - Immutable MyGigsUiState data class; ALL transient state lives here.
 *  - Sealed MyGigsAction drives every interaction via a single onAction() lambda.
 *  - filteredGigs and tabCounts are derivedStateOf — no redundant recomputation.
 *  - ManagementGigCard receives only value params + callbacks; zero internal state except menuExpanded.
 *
 * Visual Spec (Linear / Vercel inspired dark dashboard):
 *  - Top bar: "My Gigs" title, sub-label with total count, "Post New" gradient pill.
 *  - Stats banner: horizontal 4-card LazyRow (Posted, Active, Completed, Budget).
 *  - Tab strip: underline style, compact count badges, gradient indicator on active.
 *  - Gig card: asymmetric padding, category emoji strip, hairline status dot,
 *    application count pill with indigo glow, budget right-aligned.
 *  - In-Progress footer: avatar chip + "Open Chat →" teal button.
 *  - Completed footer: "Request Review" + "Re-post →" inline links.
 *  - 3-dot dropdown: MaterialTheme.colorScheme.surfaceVariant container, icon-prefixed items with semantic colors.
 *  - Empty state: centered emoji + contextual message per tab.
 *  - Loading/Error: full-bleed centred states with retry CTA.
 */
package com.abpvt.campusgig_frontend.features.gigs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.abpvt.campusgig_frontend.core.utils.GigViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.HomeViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Gig
import com.abpvt.campusgig_frontend.features.home.HomeViewModel
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.components.AvatarInitials
import com.abpvt.campusgig_frontend.ui.theme.GlowIndigo
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticErrorBg
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccessBg
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarningBg

// ─── Domain Constants ─────────────────────────────────────────────────────────

private val MY_GIGS_TABS = listOf(
    "Active"      to "open",
    "In Progress" to "in_progress",
    "Completed"   to "completed",
    "Cancelled"   to "cancelled"
)

// ─── UI State ─────────────────────────────────────────────────────────────────

private data class MyGigsUiState(
    val selectedTab: Int = 0
)

// ─── UI Actions ───────────────────────────────────────────────────────────────

private sealed interface MyGigsAction {
    data class SelectTab(val index: Int) : MyGigsAction
}

// ─── Modifier Extensions ─────────────────────────────────────────────────────

@Composable
private fun Modifier.gigManagementCard(): Modifier = this
    .fillMaxWidth()
    .clip(RoundedCornerShape(16.dp))
    .background(MaterialTheme.colorScheme.surface)
    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(16.dp))

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun relativeTime(createdAt: String): String {
    if (createdAt.isBlank()) return "Recently"
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(createdAt) ?: return "Recently"
        val diff = System.currentTimeMillis() - date.time
        val days = diff / 86_400_000
        when {
            days <= 0  -> "Today"
            days == 1L -> "Yesterday"
            days < 7   -> "${days}d ago"
            else       -> "${days / 7}w ago"
        }
    } catch (e: Exception) { "Recently" }
}

private fun categoryEmoji(category: String) = when (category.lowercase()) {
    "coding"      -> "⌨️"
    "design"      -> "🎨"
    "writing"     -> "✍️"
    "tutoring"    -> "📚"
    "photography" -> "📷"
    "video"       -> "🎬"
    "marketing"   -> "📣"
    else          -> "💼"
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun MyGigsScreen(navController: NavController) {
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).gigRepository,
            (LocalContext.current.applicationContext as CampusGigApplication).userRepository
        )
    )
    val gigViewModel: GigViewModel = viewModel(
        factory = GigViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).gigRepository
        )
    )

    val profileState by homeViewModel.currentUser.collectAsState()
    val myGigsState  by gigViewModel.myGigs.collectAsState()

    val currentUser by remember { derivedStateOf { (profileState as? Resource.Success)?.data } }

    LaunchedEffect(currentUser) {
        currentUser?.let { gigViewModel.loadMyGigs(it.id) }
    }

    var ui by remember { mutableStateOf(MyGigsUiState()) }

    val onAction: (MyGigsAction) -> Unit = { action ->
        when (action) {
            is MyGigsAction.SelectTab -> ui = ui.copy(selectedTab = action.index)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = myGigsState) {

            is Resource.Loading -> {
                GigsLoadingState()
            }

            is Resource.Error -> {
                GigsErrorState(
                    message  = state.message,
                    onRetry  = { currentUser?.let { gigViewModel.loadMyGigs(it.id) } }
                )
            }

            is Resource.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                val gigs = (state as Resource.Success<List<Gig>>).data

                // Derived — recalculates only when gigs or selectedTab changes
                val tabCounts by remember {
                    derivedStateOf {
                        MY_GIGS_TABS.map { (_, status) ->
                            gigs.count { it.status.lowercase() == status }
                        }
                    }
                }
                val filteredGigs by remember {
                    derivedStateOf {
                        val targetStatus = MY_GIGS_TABS[ui.selectedTab].second
                        gigs.filter { it.status.lowercase() == targetStatus }
                    }
                }

                // Aggregate stats
                val totalPosted  = gigs.size
                val activeNow    = gigs.count { it.status.lowercase() == "open" }
                val completed    = gigs.count { it.status.lowercase() == "completed" }
                val totalBudget  = gigs.filter { it.status.lowercase() == "completed" }.sumOf { it.budget }

                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 56.dp)
                ) {
                    // ── Top Bar ────────────────────────────────────────────────
                    item(key = "header") {
                        MyGigsTopBar(
                            totalGigs = totalPosted,
                            onBack    = { navController.popBackStack() },
                            onPostNew = { navController.navigate(Routes.CREATE_GIG) }
                        )
                    }

                    // ── Stats Horizontal Scroll ────────────────────────────────
                    item(key = "stats") {
                        Spacer(Modifier.height(16.dp))
                        GigStatsRow(
                            totalPosted = totalPosted,
                            activeNow   = activeNow,
                            completed   = completed,
                            totalBudget = totalBudget
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    // ── Tab Strip ─────────────────────────────────────────────
                    item(key = "tabs") {
                        GigTabStrip(
                            tabs       = MY_GIGS_TABS.map { it.first },
                            counts     = tabCounts,
                            selectedTab = ui.selectedTab,
                            onTabClick = { onAction(MyGigsAction.SelectTab(it)) }
                        )
                    }

                    // ── Content: empty or cards ────────────────────────────────
                    if (filteredGigs.isEmpty()) {
                        item(key = "empty") {
                            GigsEmptyState(tabLabel = MY_GIGS_TABS[ui.selectedTab].first)
                        }
                    } else {
                        items(filteredGigs, key = { it.id }) { gig ->
                            Spacer(Modifier.height(10.dp))
                            ManagementGigCard(
                                gig                = gig,
                                onViewDetail       = { navController.navigate(Routes.gigDetail(gig.id)) },
                                onViewApplications = { navController.navigate(Routes.gigApplications(gig.id)) },
                                onOpenChat         = { navController.navigate(Routes.CHAT_LIST) },
                                onEdit             = { navController.navigate(Routes.CREATE_GIG) },
                                onClose            = { /* TODO: close gig API */ },
                                onDelete           = {
                                    gigViewModel.deleteGig(gig.id) {
                                        currentUser?.let { gigViewModel.loadMyGigs(it.id) }
                                    }
                                },
                                onRequestReview    = { /* TODO: request review */ },
                                onRepost           = { navController.navigate(Routes.CREATE_GIG) }
                            )
                        }
                        item(key = "bottom_pad") { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }

            null -> Unit
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun MyGigsTopBar(
    totalGigs: Int,
    onBack:    () -> Unit,
    onPostNew: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(0.dp))
            .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Back",
                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "My Gigs",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 19.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                if (totalGigs == 0) "No gigs posted yet"
                else "$totalGigs gig${if (totalGigs == 1) "" else "s"} posted",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        // Post New pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(GradientIndigoStart, GradientIndigoMid, GradientIndigoEnd)
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onPostNew
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text(
                "Post New",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

// ─── Stats Horizontal Row ─────────────────────────────────────────────────────

private data class StatItem(
    val icon:       ImageVector,
    val iconColor:  Color,
    val iconBg:     Color,
    val label:      String,
    val value:      String
)

@Composable
private fun GigStatsRow(
    totalPosted: Int,
    activeNow:   Int,
    completed:   Int,
    totalBudget: Double
) {
    val stats = listOf(
        StatItem(Icons.Default.Work,        MaterialTheme.colorScheme.primary,      GlowIndigo,          "Posted",    totalPosted.toString()),
        StatItem(Icons.Default.Edit,        SemanticWarning, SemanticWarningBg,  "Active",    activeNow.toString()),
        StatItem(Icons.Default.CheckCircle, SemanticSuccess, SemanticSuccessBg,  "Completed", completed.toString()),
        StatItem(Icons.Default.Star,        Color(0xFFF59E0B), Color(0x1AF59E0B),"Budget",    "₹${totalBudget.toInt()}")
    )

    LazyRow(
        contentPadding        = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(stats) { stat ->
            Column(
                modifier = Modifier
                    .width(108.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon with coloured background
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(stat.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        stat.icon, null,
                        tint     = stat.iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    stat.value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    stat.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ─── Tab Strip ────────────────────────────────────────────────────────────────

@Composable
private fun GigTabStrip(
    tabs:        List<String>,
    counts:      List<Int>,
    selectedTab: Int,
    onTabClick:  (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { onTabClick(index) }
                        .padding(top = 12.dp, bottom = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            tab,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize   = 11.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        // Count badge
                        if (counts[index] > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    counts[index].toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize   = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // Active underline indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (isSelected)
                                    Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                                else
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                    )

                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    }
}

// ─── Management Gig Card ─────────────────────────────────────────────────────

@Composable
private fun ManagementGigCard(
    gig:                Gig,
    onViewDetail:       () -> Unit,
    onViewApplications: () -> Unit,
    onOpenChat:         () -> Unit,
    onEdit:             () -> Unit,
    onClose:            () -> Unit,
    onDelete:           () -> Unit,
    onRequestReview:    () -> Unit,
    onRepost:           () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val statusStr = gig.status.lowercase()
    val (statusColor, statusLabel, statusBg) = when (statusStr) {
        "open"                -> Triple(SemanticSuccess, "Active",      SemanticSuccessBg)
        "in_progress"         -> Triple(SemanticWarning, "In Progress", SemanticWarningBg)
        "completed"           -> Triple(Color(0xFF14B8A6), "Completed", Color(0x1A14B8A6))
        "cancelled", "closed" -> Triple(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),    "Cancelled",  MaterialTheme.colorScheme.surfaceContainerHigh)
        else                  -> Triple(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),    gig.status,   MaterialTheme.colorScheme.surfaceContainerHigh)
    }

    val timeAgo = remember(gig.createdAt) { relativeTime(gig.createdAt) }

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .gigManagementCard()
    ) {
        // ── Category emoji accent strip ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(categoryEmoji(gig.category), fontSize = 13.sp)
                Text(
                    gig.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    timeAgo,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                // Status dot + label
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusBg)
                        .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.25f)), RoundedCornerShape(999.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusColor
                    )
                }
            }
        }

        // ── Card Body ───────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {

            // Title + 3-dot menu row
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    gig.title,
                    style    = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        lineHeight  = 22.sp
                    ),
                    color    = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onViewDetail
                        )
                )

                // 3-dot dropdown
                Box {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null
                            ) { menuExpanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MoreVert, "Options",
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded         = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor   = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground) },
                            leadingIcon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp)) },
                            onClick = { menuExpanded = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Close Gig", style = MaterialTheme.typography.bodySmall, color = SemanticWarning) },
                            leadingIcon = { Icon(Icons.Default.Close, null, tint = SemanticWarning, modifier = Modifier.size(15.dp)) },
                            onClick = { menuExpanded = false; onClose() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", style = MaterialTheme.typography.bodySmall, color = SemanticError) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = SemanticError, modifier = Modifier.size(15.dp)) },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Application count + budget row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Application count pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(GlowIndigo)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), RoundedCornerShape(7.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onViewApplications
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.People, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Text(
                        "${gig.applicationsCount} application${if (gig.applicationsCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Budget
                Text(
                    gig.formattedBudget(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 15.sp
                    ),
                    color = SemanticSuccess
                )
            }

            // ── In Progress footer ──────────────────────────────────────────
            if (statusStr == "in_progress") {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("W", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp), color = Color.White)
                        }
                        Text(
                            "Working with Candidate",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1414B8A6))
                            .border(BorderStroke(1.dp, Color(0xFF14B8A6).copy(alpha = 0.35f)), RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = onOpenChat
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Open Chat →",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                            color = Color(0xFF14B8A6)
                        )
                    }
                }
            }

            // ── Completed footer ────────────────────────────────────────────
            if (statusStr == "completed") {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Request Review ★",
                        style    = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color    = SemanticWarning,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onRequestReview
                        )
                    )
                    Text(
                        "Re-post →",
                        style    = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onRepost
                        )
                    )
                }
            }
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun GigsEmptyState(tabLabel: String) {
    val (emoji, message) = when (tabLabel.lowercase()) {
        "active"      -> "📋" to "No active gigs right now\nPost one to get started!"
        "in progress" -> "🔧" to "No gigs in progress yet\nAccept an applicant to begin work"
        "completed"   -> "🏆" to "No completed gigs yet\nFinish a project to see it here"
        else          -> "📁" to "No $tabLabel gigs"
    }

    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(emoji, fontSize = 48.sp)
            Text(
                message,
                style     = MaterialTheme.typography.bodySmall.copy(
                    fontSize   = 14.sp,
                    lineHeight  = 21.sp
                ),
                color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ─── Full Screen Loading ──────────────────────────────────────────────────────

@Composable
private fun GigsLoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color       = MaterialTheme.colorScheme.primary,
                modifier    = Modifier.size(32.dp),
                strokeWidth = 2.5.dp
            )
            Text(
                "Loading your gigs…",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// ─── Full Screen Error ────────────────────────────────────────────────────────

@Composable
private fun GigsErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("😕", fontSize = 40.sp)
            Text(
                message,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = SemanticError
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GlowIndigo)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onRetry
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "Retry",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
