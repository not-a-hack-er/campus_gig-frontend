/**
 * MyApplicationsScreen.kt — v3.0 — Student Application Tracker
 *
 * Architecture:
 *  - Immutable MyApplicationsUiState owns selectedTab + starRating map.
 *  - Sealed MyApplicationsAction drives all interactions via onAction().
 *  - filteredApps is derivedStateOf — recomputes only on tab change or data change.
 *  - ApplicationCard is pure: no internal var state except animatables.
 *
 * Visual Spec (Linear / Notion inspired):
 *  - Top bar: title + application count sub-label.
 *  - Stats LazyRow: 4 rounded pills (Pending / Accepted / In Progress / Completed)
 *    each showing count + semantic color.
 *  - Filter tabs: horizontal scroll, gradient underline on active, hairline bottom border.
 *  - Card: left 4dp semantic accent bar, no card shadow, hairline surface border.
 *    · Status pill (live dot) + relative timestamp
 *    · Gig title (2-line clamp) + category tag
 *    · Mini poster avatar chip (initials + name)
 *    · Hairline divider
 *    · Bid amount (green) + applied date
 *    · STATUS FOOTER:
 *        pending     → pulsing amber dot + "Waiting for response"
 *        accepted    → teal success banner + "Open Chat →"
 *        in_progress → blue LinearProgressIndicator + days label
 *        completed   → 5-star inline review (rating persisted in UiState)
 *        rejected    → muted red "Find similar gigs →" card
 *        withdrawn   → "Re-apply →" (if gig still open) or grey note
 *  - Swipe-to-dismiss (pending only): red "Withdraw" background reveal.
 *  - Shimmer skeleton (4 cards) while loading.
 *  - Rich empty state: emoji + heading + body + gradient CTA button.
 */
package com.abpvt.campusgig_frontend.features.applications

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ApplicationViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Application
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.components.getRelativeTime
import com.abpvt.campusgig_frontend.ui.theme.BorderSubtle
import com.abpvt.campusgig_frontend.ui.theme.GlowIndigo
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.Indigo400
import com.abpvt.campusgig_frontend.ui.theme.Indigo500
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticErrorBg
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccessBg
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarningBg
import com.abpvt.campusgig_frontend.ui.theme.Surface1
import com.abpvt.campusgig_frontend.ui.theme.Surface2
import com.abpvt.campusgig_frontend.ui.theme.Surface3
import com.abpvt.campusgig_frontend.ui.theme.Surface4
import com.abpvt.campusgig_frontend.ui.theme.TextPrimary
import com.abpvt.campusgig_frontend.ui.theme.TextSecondary
import com.abpvt.campusgig_frontend.ui.theme.TextTertiary

// ─── Domain Constants ─────────────────────────────────────────────────────────

private val FILTER_TABS = listOf(
    "All"         to (null as String?),
    "Pending"     to "pending",
    "Accepted"    to "accepted",
    "In Progress" to "in_progress",
    "Completed"   to "completed",
    "Withdrawn"   to "withdrawn"
)

private data class StatPill(
    val label: String,
    val color: Color,
    val count: Int
)

// ─── UI State ─────────────────────────────────────────────────────────────────

private data class MyApplicationsUiState(
    val selectedTab: Int                  = 0,
    val starRatings: Map<String, Int>     = emptyMap()   // applicationId → 1–5
)

// ─── UI Actions ───────────────────────────────────────────────────────────────

private sealed interface MyApplicationsAction {
    data class SelectTab(val index: Int)                    : MyApplicationsAction
    data class SetStarRating(val appId: String, val stars: Int) : MyApplicationsAction
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    navController: NavController,
    viewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).apiService
        )
    )
) {
    val applicationsState by viewModel.applications.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMyApplications() }

    var ui by remember { mutableStateOf(MyApplicationsUiState()) }

    val onAction: (MyApplicationsAction) -> Unit = { action ->
        when (action) {
            is MyApplicationsAction.SelectTab     -> ui = ui.copy(selectedTab = action.index)
            is MyApplicationsAction.SetStarRating -> ui = ui.copy(
                starRatings = ui.starRatings + (action.appId to action.stars)
            )
        }
    }

    // derivedStateOf — only recomputes when applicationsState or selectedTab changes
    val filteredApps by remember {
        derivedStateOf {
            val targetStatus = FILTER_TABS[ui.selectedTab].second
            val all = (applicationsState as? Resource.Success<List<Application>>)?.data ?: emptyList()
            if (targetStatus == null) all else all.filter { it.status.lowercase() == targetStatus }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface1)
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 56.dp)
        ) {

            // ── Top Bar ──────────────────────────────────────────────────────
            item(key = "header") {
                AppsTopBar(
                    count  = (applicationsState as? Resource.Success<List<Application>>)?.data?.size ?: 0,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Stats Pill Row ────────────────────────────────────────────────
            if (applicationsState is Resource.Success<*>) {
                @Suppress("UNCHECKED_CAST")
                val allApps = (applicationsState as Resource.Success<List<Application>>).data
                item(key = "stats") {
                    Spacer(Modifier.height(12.dp))
                    StatPillRow(
                        pills = listOf(
                            StatPill("Pending",     SemanticWarning,      allApps.count { it.status == "pending" }),
                            StatPill("Accepted",    SemanticSuccess,      allApps.count { it.status == "accepted" }),
                            StatPill("In Progress", Color(0xFF3B82F6),    allApps.count { it.status == "in_progress" }),
                            StatPill("Completed",   Color(0xFF14B8A6),    allApps.count { it.status == "completed" }),
                        )
                    )
                }
            }

            // ── Filter Tab Row ────────────────────────────────────────────────
            item(key = "tabs") {
                Spacer(Modifier.height(4.dp))
                AppsFilterTabRow(
                    selectedTab = ui.selectedTab,
                    onTabClick  = { onAction(MyApplicationsAction.SelectTab(it)) }
                )
            }

            // ── Content ───────────────────────────────────────────────────────
            when (val state = applicationsState) {
                null, is Resource.Loading -> {
                    items(4, key = { "shimmer_$it" }) {
                        Spacer(Modifier.height(10.dp))
                        ShimmerApplicationCard()
                    }
                }
                is Resource.Error -> {
                    item(key = "error") {
                        AppErrorState(
                            message = state.message,
                            onRetry = { viewModel.loadMyApplications() }
                        )
                    }
                }
                is Resource.Success<*> -> {
                    if (filteredApps.isEmpty()) {
                        item(key = "empty") {
                            ApplicationsEmptyState(
                                tabLabel      = FILTER_TABS[ui.selectedTab].first,
                                onBrowseGigs  = { navController.navigate(Routes.GIG_LIST) }
                            )
                        }
                    } else {
                        items(filteredApps, key = { it.id }) { application ->
                            val isPending = application.status.lowercase() == "pending"
                            Spacer(Modifier.height(10.dp))

                            if (isPending) {
                                val swipeState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value != SwipeToDismissBoxValue.Settled) {
                                            viewModel.withdrawApplication(application.id)
                                            true
                                        } else false
                                    }
                                )
                                SwipeToDismissBox(
                                    state             = swipeState,
                                    modifier          = Modifier.animateItem(),
                                    backgroundContent = {
                                        val bgColor by animateColorAsState(
                                            targetValue   = if (swipeState.targetValue == SwipeToDismissBoxValue.Settled)
                                                                Surface2
                                                            else
                                                                SemanticError.copy(alpha = 0.18f),
                                            animationSpec = tween(200),
                                            label         = "swipeBg"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 20.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(bgColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "⬅ Withdraw",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = SemanticError
                                            )
                                        }
                                    }
                                ) {
                                    ApplicationCard(
                                        application   = application,
                                        starRating    = ui.starRatings[application.id] ?: 0,
                                        onSetStars    = { stars -> onAction(MyApplicationsAction.SetStarRating(application.id, stars)) },
                                        onOpenChat    = { buildChatRoute(navController, application) },
                                        onFindSimilar = { navController.navigate(Routes.GIG_LIST) },
                                        onReApply     = {
                                            application.gig?.id?.let {
                                                navController.navigate(Routes.gigDetail(it))
                                            }
                                        }
                                    )
                                }
                            } else {
                                ApplicationCard(
                                    application   = application,
                                    starRating    = ui.starRatings[application.id] ?: 0,
                                    onSetStars    = { stars -> onAction(MyApplicationsAction.SetStarRating(application.id, stars)) },
                                    onOpenChat    = { buildChatRoute(navController, application) },
                                    onFindSimilar = { navController.navigate(Routes.GIG_LIST) },
                                    onReApply     = {
                                        application.gig?.id?.let {
                                            navController.navigate(Routes.gigDetail(it))
                                        }
                                    },
                                    modifier      = Modifier.animateItem()
                                )
                            }
                        }
                        item(key = "bottom_pad") { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }
}

private fun buildChatRoute(navController: NavController, application: Application) {
    val employer = application.gig?.employer
    val id       = employer?.id ?: return
    val name     = employer.name
    navController.navigate(
        Routes.chat(
            receiverId   = id,
            receiverName = name,
            gigTitle     = application.gig?.title,
            gigBudget    = "₹${application.expectedBudget.toInt()}"
        )
    )
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun AppsTopBar(count: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface1)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(0.dp))
            .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, "Back",
                tint     = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "My Applications",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 19.sp
                ),
                color = TextPrimary
            )
            Text(
                if (count == 0) "No applications yet" else "$count total applications",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextTertiary
            )
        }
    }
}

// ─── Stat Pill Row ────────────────────────────────────────────────────────────

@Composable
private fun StatPillRow(pills: List<StatPill>) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pills) { pill ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(pill.color.copy(alpha = 0.1f))
                    .border(BorderStroke(1.dp, pill.color.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    pill.count.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 18.sp
                    ),
                    color = pill.color
                )
                Text(
                    pill.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = pill.color.copy(alpha = 0.75f)
                )
            }
        }
    }
}

// ─── Filter Tab Row ───────────────────────────────────────────────────────────

@Composable
private fun AppsFilterTabRow(
    selectedTab: Int,
    onTabClick:  (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            modifier              = Modifier.fillMaxWidth()
        ) {
            items(FILTER_TABS.size) { index ->
                val isSelected = selectedTab == index
                Column(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { onTabClick(index) }
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        FILTER_TABS[index].first,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize   = 13.sp
                        ),
                        color = if (isSelected) TextPrimary else TextTertiary
                    )
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 24.dp else 0.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (isSelected)
                                    Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                                else
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                    )
                }
            }
        }
        // Hairline bottom divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderSubtle)
        )
        Spacer(Modifier.height(10.dp))
    }
}

// ─── Application Card ─────────────────────────────────────────────────────────

@Composable
private fun ApplicationCard(
    application:  Application,
    starRating:   Int,
    onSetStars:   (Int) -> Unit,
    onOpenChat:   () -> Unit,
    onFindSimilar: () -> Unit,
    onReApply:    () -> Unit,
    modifier:     Modifier = Modifier
) {
    val status = application.status.lowercase()

    val (accentColor, statusLabel) = when (status) {
        "accepted"    -> SemanticSuccess        to "Accepted 🎉"
        "in_progress" -> Color(0xFF3B82F6)      to "In Progress"
        "completed"   -> Color(0xFF14B8A6)      to "Completed ✓"
        "rejected"    -> SemanticError          to "Not Selected"
        "withdrawn"   -> TextTertiary           to "Withdrawn"
        else          -> SemanticWarning        to "Pending"
    }

    val relativeTime = remember(application.createdAt) { getRelativeTime(application.createdAt) }

    Row(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface2)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(16.dp))
    ) {
        // Left semantic accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(if (status in listOf("accepted", "in_progress", "completed", "rejected")) 200.dp else 136.dp)
                .background(
                    Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.4f)))
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {

            // ── Row 1: Status pill + timestamp ─────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Status pill with live dot
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize   = 10.sp
                        ),
                        color = accentColor
                    )
                }
                Text(
                    relativeTime,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextTertiary
                )
            }

            // ── Row 2: Gig title + category ─────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Text(
                application.gig?.title ?: "Unknown Gig",
                style    = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
                    lineHeight  = 22.sp
                ),
                color    = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!application.gig?.category.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    application.gig!!.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextSecondary
                )
            }

            // ── Row 3: Poster mini avatar chip ──────────────────────────────
            val posterName = application.gig?.employer?.name ?: "Unknown Employer"
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Indigo500),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        posterName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 8.sp
                        ),
                        color = Color.White
                    )
                }
                Text(
                    "by $posterName",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextTertiary
                )
            }

            // ── Divider ──────────────────────────────────────────────────────
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSubtle))
            Spacer(Modifier.height(10.dp))

            // ── Row 4: Bid + applied date ────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        "₹${application.expectedBudget.toInt()}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 14.sp
                        ),
                        color = SemanticSuccess
                    )
                    Text(
                        "bid",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextTertiary
                    )
                }
                Text(
                    "Applied $relativeTime",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextTertiary
                )
            }

            // ── Status Footer ─────────────────────────────────────────────────
            Spacer(Modifier.height(10.dp))
            ApplicationStatusFooter(
                status        = status,
                application   = application,
                starRating    = starRating,
                onSetStars    = onSetStars,
                onOpenChat    = onOpenChat,
                onFindSimilar = onFindSimilar,
                onReApply     = onReApply
            )
        }
    }
}

// ─── Status Footer ────────────────────────────────────────────────────────────

@Composable
private fun ApplicationStatusFooter(
    status:       String,
    application:  Application,
    starRating:   Int,
    onSetStars:   (Int) -> Unit,
    onOpenChat:   () -> Unit,
    onFindSimilar: () -> Unit,
    onReApply:    () -> Unit
) {
    when (status) {
        "pending" -> {
            val transition = rememberInfiniteTransition(label = "pending_pulse")
            val pulseScale by transition.animateFloat(
                initialValue = 1f,
                targetValue  = 1.6f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(750, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(SemanticWarning)
                )
                Text(
                    "Waiting for employer's response…",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextTertiary
                )
            }
        }

        "accepted" -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SemanticSuccessBg)
                    .border(BorderStroke(1.dp, SemanticSuccess.copy(alpha = 0.25f)), RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onOpenChat
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "You were selected! 🎉 Congratulations",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 12.sp
                    ),
                    color = SemanticSuccess
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline, null,
                        tint     = SemanticSuccess,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        "Chat →",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize   = 11.sp
                        ),
                        color = SemanticSuccess
                    )
                }
            }
        }

        "in_progress" -> {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Work in Progress",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 11.sp
                        ),
                        color = Color(0xFF3B82F6)
                    )
                    Text(
                        "~7 days remaining",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextTertiary
                    )
                }
                LinearProgressIndicator(
                    progress  = { 0.45f },
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color      = Color(0xFF3B82F6),
                    trackColor = Surface3
                )
            }
        }

        "completed" -> {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    "Rate your experience",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 11.sp
                    ),
                    color = TextTertiary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    (1..5).forEach { starIndex ->
                        Icon(
                            if (starRating >= starIndex) Icons.Default.Star else Icons.Default.StarBorder,
                            "Star $starIndex",
                            tint     = if (starRating >= starIndex) SemanticWarning else Surface4,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication        = null
                                ) { onSetStars(starIndex) }
                        )
                    }
                    if (starRating > 0) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Submitted ✓",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = SemanticSuccess
                        )
                    }
                }
            }
        }

        "rejected" -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SemanticErrorBg)
                    .border(BorderStroke(1.dp, SemanticError.copy(alpha = 0.15f)), RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onFindSimilar
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Don't worry — keep applying!",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextTertiary
                )
                Text(
                    "Find similar →",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 11.sp
                    ),
                    color = Indigo400
                )
            }
        }

        "withdrawn" -> {
            if (application.gig?.isOpen() == true) {
                Text(
                    "This gig is still open — Re-apply →",
                    style    = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 12.sp
                    ),
                    color    = Indigo400,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onReApply
                    )
                )
            } else {
                Text(
                    "You withdrew this application",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextTertiary
                )
            }
        }
    }
}

// ─── Shimmer Skeleton Card ────────────────────────────────────────────────────

@Composable
private fun ShimmerApplicationCard() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 0.6f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "shimmer_alpha"
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface2)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(140.dp)
                .background(Surface4.copy(alpha = alpha))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.width(64.dp).height(14.dp).clip(RoundedCornerShape(7.dp)).background(Surface4.copy(alpha = alpha)))
                Box(modifier = Modifier.width(48.dp).height(12.dp).clip(RoundedCornerShape(6.dp)).background(Surface4.copy(alpha = alpha)))
            }
            Box(modifier = Modifier.fillMaxWidth(0.85f).height(18.dp).clip(RoundedCornerShape(6.dp)).background(Surface4.copy(alpha = alpha)))
            Box(modifier = Modifier.fillMaxWidth(0.45f).height(13.dp).clip(RoundedCornerShape(6.dp)).background(Surface4.copy(alpha = alpha)))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderSubtle))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.width(70.dp).height(13.dp).clip(RoundedCornerShape(6.dp)).background(Surface4.copy(alpha = alpha)))
                Box(modifier = Modifier.width(52.dp).height(12.dp).clip(RoundedCornerShape(6.dp)).background(Surface4.copy(alpha = alpha)))
            }
        }
    }
}

// ─── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun ApplicationsEmptyState(
    tabLabel:     String,
    onBrowseGigs: () -> Unit
) {
    val (emoji, title, body) = when (tabLabel.lowercase()) {
        "pending"     -> Triple("⏳", "No Pending Applications", "You haven't applied to any gigs yet, or all have been reviewed.")
        "accepted"    -> Triple("🎯", "No Accepted Applications", "Keep applying — your first acceptance is around the corner!")
        "in progress" -> Triple("🔧", "Nothing In Progress", "Applications move here once an employer accepts you.")
        "completed"   -> Triple("🏆", "No Completed Gigs", "Finish your first gig to see it here.")
        "withdrawn"   -> Triple("↩️", "No Withdrawn Applications", "You haven't withdrawn from any applications.")
        else          -> Triple("💼", "No Applications Yet", "Start applying to gigs that match your skills!")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(emoji, fontSize = 56.sp)

        Text(
            title,
            style     = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 18.sp
            ),
            color     = TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            body,
            style     = MaterialTheme.typography.bodySmall.copy(
                fontSize   = 14.sp,
                lineHeight  = 21.sp
            ),
            color     = TextSecondary,
            textAlign = TextAlign.Center
        )

        // CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(
                    Brush.linearGradient(
                        listOf(GradientIndigoStart, GradientIndigoMid, GradientIndigoEnd)
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onBrowseGigs
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Browse Gigs →",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

// ─── Error State ─────────────────────────────────────────────────────────────

@Composable
private fun AppErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("😕", fontSize = 40.sp)
            Text(
                message,
                style     = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color     = SemanticError,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GlowIndigo)
                    .border(BorderStroke(1.dp, Indigo500.copy(alpha = 0.4f)), RoundedCornerShape(10.dp))
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
                    color = Indigo400
                )
            }
        }
    }
}
