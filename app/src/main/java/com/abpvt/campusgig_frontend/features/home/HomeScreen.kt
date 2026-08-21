/**
 * HomeScreen.kt — v3.0 — The CampusVault Dashboard (Redesigned)
 *
 * Premium design spec implementation:
 *  - Sticky top bar with blur-behind effect
 *  - Time-aware greeting + quick stats row (3 equal cards)
 *  - Fake search bar (tap → SearchScreen, no keyboard flash)
 *  - Category chips (single-select, filters Recommended section)
 *  - Featured pager (88% width, peek, category gradients, dots)
 *  - Recommended gig cards (category icon, skills chips, budget chip)
 *  - Trending communities (horizontal scroll, join button)
 *  - Active applications (accent bar, status-specific content)
 *  - Staggered entry animations (60ms per section)
 */
package com.abpvt.campusgig_frontend.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ApplicationViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.HomeViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Application
import com.abpvt.campusgig_frontend.data.model.Gig
import com.abpvt.campusgig_frontend.features.applications.ApplicationViewModel
import com.abpvt.campusgig_frontend.ui.components.VLockLogoIcon
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.GradientLightCoding
import com.abpvt.campusgig_frontend.ui.theme.GradientLightDesign
import com.abpvt.campusgig_frontend.ui.theme.GradientLightWriting
import com.abpvt.campusgig_frontend.ui.theme.GradientLightTutoring
import com.abpvt.campusgig_frontend.ui.theme.GradientLightPhoto
import com.abpvt.campusgig_frontend.ui.theme.GradientLightVideo
import com.abpvt.campusgig_frontend.ui.theme.GradientLightMarketing
import com.abpvt.campusgig_frontend.ui.theme.GradientLightOther
import com.abpvt.campusgig_frontend.ui.theme.GradientTealEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientTealStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.ui.theme.ThemeViewModel
import com.abpvt.campusgig_frontend.ui.theme.Violet400
import kotlinx.coroutines.delay
import java.util.Calendar

// ─── Color Palette Helpers ────────────────────────────────────────────────────
private object CategoryColors {
    // Dark-mode card gradients (deep, rich)
    val coding      = listOf(Color(0xFF312E81), Color(0xFF4338CA))
    val design      = listOf(Color(0xFF831843), Color(0xFFBE185D))
    val writing     = listOf(Color(0xFF134E4A), Color(0xFF0D9488))
    val tutoring    = listOf(Color(0xFF78350F), Color(0xFFD97706))
    val photography = listOf(Color(0xFF7F1D1D), Color(0xFFDC2626))
    val video       = listOf(Color(0xFF1E3A5F), Color(0xFF1D4ED8))
    val marketing   = listOf(Color(0xFF4C1D95), Color(0xFF7C3AED))
    val other       = listOf(Color(0xFF1E1B4B), Color(0xFF3730A3))

    fun forCategory(cat: String, isDark: Boolean = true) = when (cat.lowercase()) {
        "coding"      -> if (isDark) coding      else GradientLightCoding
        "design"      -> if (isDark) design      else GradientLightDesign
        "writing"     -> if (isDark) writing     else GradientLightWriting
        "tutoring"    -> if (isDark) tutoring    else GradientLightTutoring
        "photography" -> if (isDark) photography else GradientLightPhoto
        "video"       -> if (isDark) video       else GradientLightVideo
        "marketing"   -> if (isDark) marketing   else GradientLightMarketing
        else          -> if (isDark) other       else GradientLightOther
    }

    // Icon container bg + icon color — Duolingo-inspired 8 distinct rich colors
    data class IconStyle(val bg: Color, val icon: Color)
    fun iconStyle(cat: String, isDark: Boolean = true) = when (cat.lowercase()) {
        "coding"      -> if (isDark) IconStyle(Color(0x336366F1), Color(0xFF818CF8))
                         else        IconStyle(Color(0xFFEEF2FF), Color(0xFF4F46E5))  // Electric Indigo
        "design"      -> if (isDark) IconStyle(Color(0x33EC4899), Color(0xFFF472B6))
                         else        IconStyle(Color(0xFFFFF0F3), Color(0xFFE23744))  // Zomato Coral
        "writing"     -> if (isDark) IconStyle(Color(0x3314B8A6), Color(0xFF2DD4BF))
                         else        IconStyle(Color(0xFFD1FAE5), Color(0xFF059669))  // Forest Emerald
        "tutoring"    -> if (isDark) IconStyle(Color(0x33F59E0B), Color(0xFFFCD34D))
                         else        IconStyle(Color(0xFFFFFBEB), Color(0xFFD97706))  // Rich Amber
        "photography" -> if (isDark) IconStyle(Color(0x33EF4444), Color(0xFFF87171))
                         else        IconStyle(Color(0xFFFDF4FF), Color(0xFF9333EA))  // Grape Purple
        "video"       -> if (isDark) IconStyle(Color(0x333B82F6), Color(0xFF60A5FA))
                         else        IconStyle(Color(0xFFE0F2FE), Color(0xFF0284C7))  // Sky Blue
        "marketing"   -> if (isDark) IconStyle(Color(0x33A855F7), Color(0xFFC084FC)  )
                         else        IconStyle(Color(0xFFFFF7ED), Color(0xFFEA580C))  // Swiggy Orange
        else          -> if (isDark) IconStyle(Color(0x33647483), Color(0xFF94A3B8))
                         else        IconStyle(Color(0xFFF5F3FF), Color(0xFF7C3AED))  // Violet
    }
}

// --- Mock / Static Data ----------------------------------------------------------
// Featured banner cards (static UI showcase - tap navigates to real gig list)
private data class FeaturedCard(
    val category: String,
    val title: String,
    val budget: String,
    val poster: String,
    val gigId: String? = null
)

private val featuredCards = listOf(
    FeaturedCard("Coding",      "Build a Full-Stack E-Commerce Platform",   "₹8,000 - 12,000", "Rohan M."),
    FeaturedCard("Design",      "Brand Identity for a Tech Startup",         "₹5,000 - 7,000",  "Priya K."),
    FeaturedCard("Writing",     "SEO Blog Articles - 10 Posts",              "₹3,000 - 4,000",  "Arjun S."),
    FeaturedCard("Photography", "Product Photography for D2C Brand",         "₹2,500 - 4,000",  "Neha R."),
)

// Community cards (static - real communities shown on Communities screen)
private data class CommunityCard(
    val name: String,
    val members: String,
    val category: String,
    val joined: Boolean = false,
    val gradientColors: List<Color>
)

private val trendingCommunities = listOf(
    CommunityCard("Dev Circle",      "2.4K", "Coding",  false, listOf(Color(0xFF312E81), Color(0xFF6366F1))),
    CommunityCard("Design Hub",      "1.8K", "Design",  true,  listOf(Color(0xFF831843), Color(0xFFBE185D))),
    CommunityCard("Freelance Pro",   "3.1K", "General", false, listOf(Color(0xFF134E4A), Color(0xFF0D9488))),
    CommunityCard("Campus Creators", "956",  "Video",   true,  listOf(Color(0xFF4C1D95), Color(0xFF7C3AED))),
)

private val categoryList = listOf("All", "Coding", "Design", "Writing", "Tutoring", "Photography", "Video", "Marketing", "Other")


// ─── Home Screen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel? = null,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).gigRepository,
            (LocalContext.current.applicationContext as CampusGigApplication).userRepository,
            (LocalContext.current.applicationContext as CampusGigApplication).apiService
        )
    )
) {
    val appViewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).apiService
        )
    )

    val profileState by viewModel.currentUser.collectAsState()
    val gigsState    by viewModel.featuredGigs.collectAsState()
    val appsState    by appViewModel.applications.collectAsState()
    val unreadCount  by viewModel.unreadCount.collectAsState()

    // Observe current theme for adaptive color choices
    val isDarkTheme  by (themeViewModel?.isDarkTheme ?: kotlinx.coroutines.flow.MutableStateFlow(true)).collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Reload user profile and active applications on resume (picks up profile edits/applications instantly)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadHomeData()
                appViewModel.loadMyApplications()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Real-time socket notification listener for instant updates while home is visible
        com.abpvt.campusgig_frontend.features.chat.SocketManager.onNewNotification { notification ->
            viewModel.loadHomeData()
            appViewModel.loadMyApplications()
            scope.launch {
                val title = notification.optString("title", "New Notification")
                val body = notification.optString("body", "")
                snackbarHostState.showSnackbar("$title\n$body")
            }
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            com.abpvt.campusgig_frontend.features.chat.SocketManager.removeNotificationListener()
        }
    }

    val userName = (profileState as? Resource.Success<com.abpvt.campusgig_frontend.data.model.User>)?.data?.name?.split(" ")?.firstOrNull() ?: "there"
    val currentUser = (profileState as? Resource.Success<com.abpvt.campusgig_frontend.data.model.User>)?.data

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when { hour < 12 -> "Good morning"; hour < 17 -> "Good afternoon"; else -> "Good evening" }
    }

    var selectedCategory by remember { mutableStateOf("All") }
    val liveGigs = (gigsState as? Resource.Success<List<Gig>>)?.data
    val activeFeaturedCards = remember(liveGigs) {
        if (!liveGigs.isNullOrEmpty()) {
            liveGigs.take(5).map { gig ->
                FeaturedCard(
                    category = gig.category.ifBlank { "General" },
                    title = gig.title,
                    budget = gig.formattedBudget(),
                    poster = gig.employer?.name ?: "Campus Member",
                    gigId = gig.id
                )
            }
        } else {
            featuredCards
        }
    }
    val pagerState = rememberPagerState(pageCount = { activeFeaturedCards.size })

    // ── Staggered entry animation state ──────────────────────────────────────
    val sectionCount = 7
    val sectionVisible = remember { Array(sectionCount) { mutableStateOf(false) } }
    LaunchedEffect(Unit) {
        sectionVisible.forEachIndexed { i, state ->
            delay(60L * i)
            state.value = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {

            // ── 1. Sticky Top Bar ─────────────────────────────────────────────
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(0.dp))
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .zIndex(10f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Official CampusVault V-Lock Logo Mark
                        VLockLogoIcon(size = 30.dp)

                        Spacer(Modifier.width(10.dp))
                        Text(
                            "CampusVault",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.weight(1f))

                        // Search icon
                        IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                            Icon(Icons.Default.Search, "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                        }

                        // Theme toggle ☀️/🌙
                        IconButton(onClick = { themeViewModel?.toggleTheme() }) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Bell with badge (only show if there are unread notifications)
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = SemanticError, modifier = Modifier.size(8.dp))
                                }
                            }
                        ) {
                            IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                                Icon(Icons.Default.Notifications, "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }

            // ── 2. Greeting + Quick Stats ─────────────────────────────────────
            item {
                SectionWrapper(visible = sectionVisible[0].value) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(Modifier.height(24.dp))

                        Text(
                            "$greeting, $userName 👋",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.SemiBold, fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "3 new gigs match your skills",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(20.dp))

                        // Quick Stats Row — real data from user profile
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Count pending/active applications
                            val activeAppsCount = (appsState as? Resource.Success)?.data
                                ?.count { it.status == "pending" || it.status == "accepted" } ?: 0

                            QuickStatCard(
                                value = "$activeAppsCount",
                                label = "Active apps",
                                icon = Icons.Default.Work,
                                iconColor = GradientTealEnd,
                                modifier = Modifier.weight(1f)
                            )
                            QuickStatCard(
                                value = if ((currentUser?.completedGigsCount ?: 0) > 0)
                                    "${currentUser!!.completedGigsCount} done" else "0 done",
                                label = "Gigs done",
                                icon = Icons.Default.Groups,
                                iconColor = SemanticSuccess,
                                modifier = Modifier.weight(1f)
                            )
                            QuickStatCard(
                                value = if ((currentUser?.reviewCount ?: 0) > 0)
                                    "%.1f ★".format(currentUser!!.rating) else "— ★",
                                label = "Avg. rating",
                                icon = Icons.Default.Star,
                                iconColor = SemanticWarning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }

            // ── 3. Search Bar (fake tap target) ───────────────────────────────
            item {
                SectionWrapper(visible = sectionVisible[1].value) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(26.dp))
                            .clickable { navController.navigate(Routes.SEARCH) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Search gigs, people, communities...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── 4. Category Chips ─────────────────────────────────────────────
            item {
                SectionWrapper(visible = sectionVisible[2].value) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categoryList) { category ->
                            val isSelected = selectedCategory == category
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    category,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium, fontSize = 13.sp
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            // ── 5. Featured Gig Banner ────────────────────────────────────────
            item {
                SectionWrapper(visible = sectionVisible[3].value) {
                    Column {
                        Text(
                            "FEATURED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium, fontSize = 12.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                        )

                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(start = 20.dp, end = 48.dp),
                            pageSpacing = 12.dp
                        ) { page ->
                            val card = activeFeaturedCards.getOrNull(page) ?: featuredCards[0]
                            FeaturedGigCard(
                                card = card,
                                onClick = {
                                    if (card.gigId != null) {
                                        navController.navigate(Routes.gigDetail(card.gigId))
                                    } else {
                                        navController.navigate(Routes.GIG_LIST)
                                    }
                                },
                                isDark = isDarkTheme
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Page indicator dots
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(activeFeaturedCards.size) { index ->
                                val isActive = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(if (isActive) 20.dp else 6.dp, 6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (isActive)
                                                Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                                            else
                                                Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
                                        )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(28.dp))
                }
            }

            // ── 6. Recommended for You ────────────────────────────────────────
            item {
                SectionWrapper(visible = sectionVisible[4].value) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recommended for You",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "See all →",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { navController.navigate(Routes.GIG_LIST) }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Recommended gig cards
            when (val state = gigsState) {
                is Resource.Loading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    }
                }
                is Resource.Success<List<Gig>> -> {
                    val allGigs = state.data
                    val appliedGigIds = (appsState as? Resource.Success)?.data?.map { it.gig?.id }?.toSet() ?: emptySet()
                    val filteredGigs = allGigs.filter { gig ->
                        gig.id !in appliedGigIds && (selectedCategory == "All" || gig.category.lowercase() == selectedCategory.lowercase())
                    }
                    val recommendedGigs = filteredGigs.take(5)

                    if (recommendedGigs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No gigs in this category yet", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        items(recommendedGigs, key = { it.id }) { gig ->
                            RecommendedGigCard(
                                gig = gig,
                                onClick = { navController.navigate(Routes.gigDetail(gig.id)) },
                                modifier = Modifier.padding(horizontal = 20.dp),
                                isDark = isDarkTheme
                            )
                            Spacer(Modifier.height(12.dp))
                        }

                        if (allGigs.size > 5) {
                            item {
                                OutlinedButton(
                                    onClick = { navController.navigate(Routes.GIG_LIST) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Text(
                                        "See more recommendations",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
                is Resource.Error -> item {
                    Text(
                        "Couldn't load gigs",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                null -> {}
            }

            // ── 7. Communities (Coming Soon) ──────────────────────────────────
            item {
                SectionWrapper(visible = sectionVisible[5].value) {
                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Communities",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.linearGradient(listOf(GradientIndigoStart, Violet400))
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "Coming Soon",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold, fontSize = 10.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    // Premium Coming Soon Banner Card for Communities
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF2E2A72), Color(0xFF4C1D95))
                                )
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // Header Row: Icon + Title/Description
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👥", fontSize = 20.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Campus Peer Hubs & Clubs",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold, fontSize = 15.sp
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "Connect, collaborate & share gig opportunities with student peers",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 15.sp),
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // 2x2 Grid of Upcoming Community Hubs (perfect alignment, zero overflow)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CommunityPill("💻 Dev Circle", modifier = Modifier.weight(1f))
                                CommunityPill("🎨 Design Hub", modifier = Modifier.weight(1f))
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CommunityPill("✍️ Freelance Pro", modifier = Modifier.weight(1f))
                                CommunityPill("📹 Creators Hub", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // ── 8. Active Applications ─────────────────────────────────────────
            item {
                SectionWrapper(visible = sectionVisible[6].value) {
                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Active Applications",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "View all →",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { navController.navigate(Routes.MY_APPLICATIONS) }
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Real applications from backend
                    when (val appsResource = appsState) {
                        is Resource.Loading -> {
                            Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        is Resource.Success -> {
                            val activeApps = appsResource.data
                                .filter { it.status == "pending" || it.status == "accepted" || it.status == "in_progress" }
                                .take(3)
                            if (activeApps.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No active applications. Start applying! 🚀",
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                activeApps.forEach { app ->
                                    RealApplicationCard(
                                        app = app,
                                        onOpenChat = {
                                            val gigPosterId = app.gig?.employer?.id ?: ""
                                            val gigPosterName = app.gig?.employer?.name ?: "Poster"
                                            if (gigPosterId.isNotBlank()) {
                                                navController.navigate(Routes.chat(gigPosterId, gigPosterName))
                                            }
                                        }
                                    )
                                    Spacer(Modifier.height(10.dp))
                                }
                            }
                        }
                        is Resource.Error -> {
                            Text("Couldn't load applications",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp))
                        }
                        null -> {}
                    }
                }
            }

            // ── 9. Bottom padding ─────────────────────────────────────────────
            item { Spacer(Modifier.height(48.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

// ─── Section Wrapper with staggered fade+slide animation ─────────────────────
@Composable
private fun SectionWrapper(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }
    ) {
        Column {
            content()
        }
    }
}

// ─── Quick Stat Card ──────────────────────────────────────────────────────────
@Composable
private fun QuickStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 18.sp), color = MaterialTheme.colorScheme.onBackground)
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

// ─── Featured Gig Card ────────────────────────────────────────────────────────
@Composable
private fun FeaturedGigCard(card: FeaturedCard, onClick: () -> Unit, isDark: Boolean = true) {
    val gradientColors = CategoryColors.forCategory(card.category, isDark)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(colors = gradientColors, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(800f, 600f)))
            .clickable { onClick() }
    ) {
        // Top row: FEATURED badge + category label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    "FEATURED",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 10.sp),
                    color = Color.White
                )
            }
            Text(
                card.category,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.75f)
            )
        }

        // Middle: Title + Budget pill
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 12.dp)
                .padding(top = 24.dp)
        ) {
            Text(
                card.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(card.budget, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
            }
        }

        // Bottom row: Poster + View Gig button
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        card.poster.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = Color.White
                    )
                }
                Text("by ${card.poster.split(" ").firstOrNull() ?: card.poster}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = Color.White.copy(alpha = 0.8f))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("View Gig →", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp), color = gradientColors.first())
            }
        }
    }
}

// ─── Recommended Gig Card ─────────────────────────────────────────────────────
@Composable
fun RecommendedGigCard(gig: Gig, onClick: () -> Unit, modifier: Modifier = Modifier, isDark: Boolean = true) {
    val catKey    = gig.category.lowercase()
    val iconStyle = CategoryColors.iconStyle(catKey, isDark)
    var isSaved by remember { mutableStateOf(false) }

    val relativeTime = remember(gig.createdAt) {
        if (gig.createdAt.isBlank()) "Recently"
        else {
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = sdf.parse(gig.createdAt) ?: return@remember "Recently"
                val diff = System.currentTimeMillis() - date.time
                val hours = diff / 3_600_000; val days = diff / 86_400_000
                when { hours < 1 -> "Just now"; hours < 24 -> "${hours}h ago"; else -> "${days}d ago" }
            } catch (e: Exception) { "Recently" }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            // ROW 1: Category icon + category label + time + bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconStyle.bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Code, null, tint = iconStyle.icon, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    gig.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(relativeTime, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    "Save",
                    tint = if (isSaved) SemanticWarning else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { isSaved = !isSaved }
                )
            }

            // ROW 2: Gig title
            Spacer(Modifier.height(10.dp))
            Text(
                gig.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ROW 3: Skills chips
            if (gig.skills.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    gig.skills.take(3).forEach { skill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(skill, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (gig.skills.size > 3) {
                        Text("+${gig.skills.size - 3} more", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // DIVIDER
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
            Spacer(Modifier.height(10.dp))

            // ROW 4: Poster info + budget chip + applied count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Poster avatar
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconStyle.bg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        gig.employer?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = iconStyle.icon
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    "by ${gig.employer?.name?.split(" ")?.firstOrNull() ?: "Anonymous"}",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.weight(1f))

                // Applied count
                Text(
                    "${gig.applicationsCount} applied",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))

                // Budget chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x1A10B981))
                        .border(1.dp, Color(0x4D10B981), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "₹${gig.budget.toInt()}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                        color = SemanticSuccess
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityPill(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(0.5.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.5.sp, fontWeight = FontWeight.Medium
            ),
            color = Color.White.copy(alpha = 0.95f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Community Mini Card ──────────────────────────────────────────────────────
@Composable
private fun CommunityMiniCard(community: CommunityCard, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(160.dp, 120.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(community.gradientColors))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        // Category badge top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(community.category, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White)
        }

        // Community info bottom-left
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(community.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = Color.White)
            Text("${community.members} members", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Color.White.copy(alpha = 0.7f))
        }

        // Join button bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color.White.copy(alpha = if (community.joined) 0.2f else 0.4f), RoundedCornerShape(6.dp))
                .background(if (community.joined) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                if (community.joined) "Joined ✓" else "Join",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp),
                color = Color.White.copy(alpha = if (community.joined) 0.5f else 1f)
            )
        }
    }
}

// ─── Active Application Card ─────────────────────────────────────────────────
// --- Real Application Card (uses live Application data from backend) -------------
@Composable
private fun RealApplicationCard(app: Application, onOpenChat: () -> Unit) {
    val statusStr   = (app.status ?: "pending").lowercase()
    val accentColor = when (statusStr) {
        "accepted"    -> SemanticSuccess
        "in_progress" -> Color(0xFF3B82F6)
        "rejected"    -> SemanticError
        else          -> SemanticWarning
    }
    val statusLabel = when (statusStr) {
        "accepted"    -> "Accepted"
        "in_progress" -> "In Progress"
        "rejected"    -> "Not Selected"
        else          -> "Pending"
    }

    val gigTitle   = app.gig?.title ?: "Gig Application"
    val appliedAgo = remember(app.createdAt) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = sdf.parse(app.createdAt) ?: return@remember "Recently"
            val days = ((System.currentTimeMillis() - date.time) / 86_400_000).toInt()
            when { days == 0 -> "Today"; days == 1 -> "Yesterday"; else -> "${days}d ago" }
        } catch (e: Exception) { "Recently" }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (statusStr == "accepted") 106.dp else 80.dp)
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        gigTitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp),
                            color = accentColor
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "Applied $appliedAgo",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (statusStr == "accepted") {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(SemanticSuccess.copy(alpha = 0.1f))
                            .border(1.dp, SemanticSuccess.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .clickable { onOpenChat() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "You were selected! 🎉  Open Chat →",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                            color = SemanticSuccess
                        )
                    }
                }
            }
        }
    }
}
