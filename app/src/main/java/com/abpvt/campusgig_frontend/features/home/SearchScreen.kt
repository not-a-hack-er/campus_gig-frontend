/**
 * SearchScreen.kt — v1.0 — Global Search & Discovery
 *
 * CONCEPT: Search that feels instant and intelligent. Not just a text box —
 * a discovery engine. Shows contextual suggestions before typing, recent
 * searches, and live results as the user types.
 *
 * States:
 * 1. PRE-SEARCH: Recent chips + Trending on Campus + Browse Categories grid
 * 2. TYPING: Suggestion chips + Tab bar + Results
 * 3. EMPTY: Illustration + "No results" + suggestions
 *
 * Filter bottom sheet: category, budget range, location type, sort
 */
package com.abpvt.campusgig_frontend.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.ui.theme.BackgroundBase
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val trendingSearches = listOf(
    "Android developer", "Logo design", "Content writing",
    "Figma UI/UX", "Python tutor", "Video editing"
)

private val categoryGrid = listOf(
    "💻" to "Coding", "🎨" to "Design", "✍️" to "Writing",
    "📚" to "Tutoring", "📷" to "Photography", "🎬" to "Video",
    "🎵" to "Music", "📊" to "Finance", "🌐" to "Marketing"
)

private val tabs = listOf("All", "Gigs", "People", "Communities")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(navController: NavController) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val recentSearches = remember { mutableStateListOf("Kotlin developer", "Figma design") }
    var showFilters by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Auto-focus on enter
    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    val isTyping = query.isNotBlank()

    Box(modifier = Modifier.fillMaxSize().background(Surface1)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface1)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Surface3)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Search input — transparent, minimal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface3)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = if (query.isNotBlank()) Indigo400 else TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(fontSize = 15.sp, color = TextPrimary),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            cursorBrush = SolidColor(Indigo400),
                            decorationBox = { inner ->
                                Box {
                                    if (query.isEmpty()) Text("Search gigs, people, communities...", style = TextStyle(fontSize = 15.sp, color = TextTertiary))
                                    inner()
                                }
                            }
                        )
                        if (query.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextTertiary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { query = "" }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Filter icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (showFilters) Indigo400.copy(alpha = 0.2f) else Surface3)
                        .border(1.dp, if (showFilters) Indigo400.copy(alpha = 0.5f) else BorderSubtle, RoundedCornerShape(10.dp))
                        .clickable { showFilters = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint = if (showFilters) Indigo400 else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Indigo400,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }

            // ── Tab Bar (visible when typing) ─────────────────────────────────
            AnimatedVisibility(visible = isTyping, enter = fadeIn(), exit = fadeOut()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface1)
                        .padding(horizontal = 16.dp)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isActive = selectedTab == index
                        Column(
                            modifier = Modifier
                                .padding(end = 20.dp, bottom = 0.dp)
                                .clickable { selectedTab = index },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = if (isActive) Indigo400 else TextTertiary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(if (isActive) 24.dp else 0.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(
                                        Brush.linearGradient(colors = listOf(GradientIndigoStart, GradientIndigoEnd))
                                    )
                            )
                        }
                    }
                }
            }

            // ── Body ──────────────────────────────────────────────────────────
            if (!isTyping) {
                // PRE-SEARCH STATE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // Recent searches
                    if (recentSearches.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recent Searches", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                            Text("Clear", style = MaterialTheme.typography.labelMedium, color = Indigo400,
                                modifier = Modifier.clickable { recentSearches.clear() })
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentSearches.forEach { recent ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Surface3)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable { query = recent }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(recent, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    }

                    // Trending on Campus
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Trending on Campus", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    trendingSearches.forEachIndexed { index, search ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { query = search }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank number with gradient text
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GradientIndigoStart.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Indigo400
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(search, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Browse Categories
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Browse Categories", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    val rows = categoryGrid.chunked(3)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { (emoji, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Surface3)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                        .clickable { query = label }
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(emoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            } else {
                // RESULTS STATE (mock)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Showing results for",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary
                        )
                        Text(
                            text = "\"$query\"",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Search API will be wired here",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }

    // ── Filter Bottom Sheet ────────────────────────────────────────────────────
    if (showFilters) {
        var selectedLocationFilter by remember { mutableStateOf("All") }
        var selectedSortFilter by remember { mutableStateOf("Newest") }

        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = filterSheetState,
            containerColor = Surface3
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Filters", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                    Text("Reset", style = MaterialTheme.typography.bodyMedium, color = Indigo400,
                        modifier = Modifier.clickable { selectedLocationFilter = "All"; selectedSortFilter = "Newest" })
                }
                Spacer(modifier = Modifier.height(20.dp))

                Text("Location Type", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Remote", "On Campus", "Hybrid").forEach { loc ->
                        val sel = selectedLocationFilter == loc
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (sel) Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)) else Brush.linearGradient(listOf(Surface2, Surface2)))
                                .border(1.dp, if (sel) Color.Transparent else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { selectedLocationFilter = loc }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(loc, style = MaterialTheme.typography.bodySmall, color = if (sel) Color.White else TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Sort By", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))
                listOf("Newest", "Budget ↑", "Budget ↓", "Deadline").forEach { sort ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSortFilter = sort }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selectedSortFilter == sort) Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)) else Brush.linearGradient(listOf(Surface2, Surface2)))
                                .border(1.dp, if (selectedSortFilter == sort) Color.Transparent else BorderSubtle, RoundedCornerShape(9.dp))
                        ) {
                            if (selectedSortFilter == sort) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(sort, style = MaterialTheme.typography.bodyMedium, color = if (selectedSortFilter == sort) TextPrimary else TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Apply button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(colors = listOf(GradientIndigoStart, GradientIndigoEnd)))
                        .clickable {
                            scope.launch { filterSheetState.hide() }
                            showFilters = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Show Results", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
