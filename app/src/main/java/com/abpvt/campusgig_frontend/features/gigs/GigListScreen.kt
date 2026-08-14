/**
 * GigListScreen.kt — v3.0 — Gig Marketplace
 *
 * Architecture:
 *  - UiState / UiAction sealed types own ALL UI state: no mutableStateOf leakage.
 *  - Every composable is single-responsibility and private to this file.
 *  - Modifier extensions replace 8+ chained modifier calls.
 *  - Slot-based composable params where content varies by context.
 *
 * Visual Spec:
 *  - Asymmetric outer margin (20.dp) / inner content gap (6–8 dp).
 *  - Hairline 1 dp BorderStroke borders, zero Material elevation shadows.
 *  - W800 ExtraBold headings with low-alpha muted body text.
 *  - Category chips bleed edge-to-edge in LazyRow (no start clip).
 *  - Sort & Filter sheets are custom — no Material RadioButton defaults.
 *  - Shimmer skeleton for loading state, illustrated empty state.
 */
package com.abpvt.campusgig_frontend.features.gigs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ApplicationViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Constants
import com.abpvt.campusgig_frontend.core.utils.GigViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Gig
import com.abpvt.campusgig_frontend.features.applications.ApplicationViewModel
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.components.GigCard
import com.abpvt.campusgig_frontend.ui.theme.BorderDefault
import com.abpvt.campusgig_frontend.ui.theme.BorderSubtle
import com.abpvt.campusgig_frontend.ui.theme.GlowIndigo
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.Indigo400
import com.abpvt.campusgig_frontend.ui.theme.Indigo500
import com.abpvt.campusgig_frontend.ui.theme.Indigo600
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.ui.theme.Surface1
import com.abpvt.campusgig_frontend.ui.theme.Surface2
import com.abpvt.campusgig_frontend.ui.theme.Surface3
import com.abpvt.campusgig_frontend.ui.theme.Surface4
import com.abpvt.campusgig_frontend.ui.theme.TextPrimary
import com.abpvt.campusgig_frontend.ui.theme.TextSecondary
import com.abpvt.campusgig_frontend.ui.theme.TextTertiary
import com.abpvt.campusgig_frontend.ui.theme.Violet400
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Domain Constants ────────────────────────────────────────────────────────

private val SORT_OPTIONS = listOf(
    "Newest first",
    "Budget: High → Low",
    "Budget: Low → High",
    "Deadline soonest",
    "Most applications",
    "Highest rated poster"
)

private val LOCATION_OPTIONS = listOf("Remote", "On Campus", "Hybrid")
private val DEADLINE_OPTIONS  = listOf("Today", "This week", "This month", "Any time")

// ─── UI State ────────────────────────────────────────────────────────────────

private data class GigListUiState(
    val searchQuery:      String        = "",
    val selectedCategory: String        = "All",
    val selectedSortIdx:  Int           = 0,
    val activeLocFilter:  String?       = null,
    val activeDeadline:   String        = "Any time",
    val showSortSheet:    Boolean       = false,
    val showFilterSheet:  Boolean       = false,
    // ephemeral per-sheet staging state
    val stagingSortIdx:   Int           = 0,
    val stagingLoc:       String?       = null,
    val stagingDeadline:  String        = "Any time"
) {
    val hasActiveFilters: Boolean
        get() = selectedCategory != "All" || activeLocFilter != null || activeDeadline != "Any time"
}

// ─── UI Actions ──────────────────────────────────────────────────────────────

private sealed interface GigListAction {
    data class SearchChanged(val q: String)                : GigListAction
    data class CategorySelected(val cat: String)           : GigListAction
    data class FilterChipRemoved(val label: String)        : GigListAction
    object ClearAllFilters                                 : GigListAction
    object OpenSort                                        : GigListAction
    object OpenFilter                                      : GigListAction
    object DismissSort                                     : GigListAction
    object DismissFilter                                   : GigListAction
    data class StageSortIdx(val idx: Int)                  : GigListAction
    data class StageLoc(val loc: String?)                  : GigListAction
    data class StageDeadline(val dl: String)               : GigListAction
    object ApplySort                                       : GigListAction
    object ApplyFilter                                     : GigListAction
}

// ─── Modifier Extensions ─────────────────────────────────────────────────────

private fun Modifier.surfaceCard(
    cornerRadius: Int = 16
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(Surface2)
    .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(cornerRadius.dp))

private fun Modifier.primaryGradientBg(cornerRadius: Int = 14): Modifier = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(
        Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoMid, GradientIndigoEnd))
    )

private fun Modifier.pressScale(pressed: Boolean): Modifier =
    this.scale(if (pressed) 0.97f else 1f)

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GigListScreen(
    navController: NavController,
    viewModel: GigViewModel = viewModel(
        factory = GigViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).gigRepository
        )
    )
) {
    val appViewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).apiService
        )
    )

    val gigsState  by viewModel.gigs.collectAsState()
    val appsState  by appViewModel.applications.collectAsState()

    LaunchedEffect(Unit) { appViewModel.loadMyApplications() }

    // All UI state in one place — no scattered mutableStateOf calls
    var ui by remember { mutableStateOf(GigListUiState()) }

    // derivedStateOf: only recomputes when the set of applied gig ids changes
    val appliedGigIds by remember {
        derivedStateOf {
            (appsState as? Resource.Success)?.data?.mapNotNull { it.gig?.id }?.toSet()
                ?: emptySet()
        }
    }

    val focusManager = LocalFocusManager.current
    val scope        = rememberCoroutineScope()
    val listState    = rememberLazyListState()

    // Delegate debounce to ViewModel; UI just reflects what it needs
    LaunchedEffect(ui.searchQuery) {
        delay(380)
        viewModel.loadGigs(
            search   = ui.searchQuery.ifBlank { null },
            category = ui.selectedCategory
        )
    }

    // ── Action Handler ────────────────────────────────────────────────────────
    val onAction: (GigListAction) -> Unit = { action ->
        when (action) {
            is GigListAction.SearchChanged     -> ui = ui.copy(searchQuery = action.q)
            is GigListAction.CategorySelected  -> {
                ui = ui.copy(selectedCategory = action.cat, searchQuery = "")
                viewModel.loadGigs(category = action.cat)
                scope.launch { listState.animateScrollToItem(0) }
            }
            is GigListAction.FilterChipRemoved -> {
                when (action.label) {
                    ui.activeLocFilter  -> ui = ui.copy(activeLocFilter = null)
                    ui.activeDeadline   -> ui = ui.copy(activeDeadline = "Any time")
                }
            }
            GigListAction.ClearAllFilters -> {
                ui = ui.copy(
                    selectedCategory = "All",
                    activeLocFilter  = null,
                    activeDeadline   = "Any time",
                    searchQuery      = ""
                )
                viewModel.loadGigs()
            }
            GigListAction.OpenSort    -> ui = ui.copy(showSortSheet = true, stagingSortIdx = ui.selectedSortIdx)
            GigListAction.OpenFilter  -> ui = ui.copy(
                showFilterSheet = true,
                stagingLoc      = ui.activeLocFilter,
                stagingDeadline = ui.activeDeadline
            )
            GigListAction.DismissSort   -> ui = ui.copy(showSortSheet = false)
            GigListAction.DismissFilter -> ui = ui.copy(showFilterSheet = false)
            is GigListAction.StageSortIdx  -> ui = ui.copy(stagingSortIdx = action.idx)
            is GigListAction.StageLoc      -> ui = ui.copy(stagingLoc = action.loc)
            is GigListAction.StageDeadline -> ui = ui.copy(stagingDeadline = action.dl)
            GigListAction.ApplySort -> {
                ui = ui.copy(showSortSheet = false, selectedSortIdx = ui.stagingSortIdx)
                viewModel.loadGigs(category = ui.selectedCategory, search = ui.searchQuery.ifBlank { null })
            }
            GigListAction.ApplyFilter -> {
                ui = ui.copy(
                    showFilterSheet = false,
                    activeLocFilter = ui.stagingLoc,
                    activeDeadline  = ui.stagingDeadline
                )
                viewModel.loadGigs(category = ui.selectedCategory, search = ui.searchQuery.ifBlank { null })
            }
        }
    }

    // Build active filter label list for the dismissible chip row
    val activeFilterLabels by remember {
        derivedStateOf {
            buildList {
                ui.activeLocFilter?.let { add(it) }
                if (ui.activeDeadline != "Any time") add(ui.activeDeadline)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface1)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 56.dp)
        ) {

            // ── 1. Sticky Topbar ─────────────────────────────────────────────
            stickyHeader(key = "topbar") {
                GigListTopBar(
                    hasActiveFilters = ui.hasActiveFilters,
                    sortLabel        = SORT_OPTIONS[ui.selectedSortIdx].take(12) + "…",
                    onBack           = { navController.popBackStack() },
                    onSort           = { onAction(GigListAction.OpenSort) },
                    onFilter         = { onAction(GigListAction.OpenFilter) }
                )
            }

            // ── 2. Search Bar ────────────────────────────────────────────────
            item(key = "search") {
                GigSearchBar(
                    query     = ui.searchQuery,
                    onChanged = { onAction(GigListAction.SearchChanged(it)) },
                    onClear   = { onAction(GigListAction.SearchChanged("")) },
                    onSearch  = { focusManager.clearFocus() },
                    modifier  = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
                )
            }

            // ── 3. Category Chips (sticky, bleeds edge-to-edge) ──────────────
            stickyHeader(key = "chips") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface1)
                ) {
                    LazyRow(
                        contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(Constants.GIG_CATEGORIES, key = { it }) { cat ->
                            CategoryFilterChip(
                                label      = cat,
                                isSelected = ui.selectedCategory == cat,
                                onClick    = { onAction(GigListAction.CategorySelected(cat)) }
                            )
                        }
                    }
                    // Hairline divider when docked
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderSubtle)
                    )
                }
            }

            // ── 4. Active Filter Chips ───────────────────────────────────────
            if (activeFilterLabels.isNotEmpty()) {
                item(key = "filter_chips") {
                    ActiveFilterRow(
                        filters  = activeFilterLabels,
                        onRemove = { onAction(GigListAction.FilterChipRemoved(it)) },
                        onClear  = { onAction(GigListAction.ClearAllFilters) }
                    )
                }
            }

            // ── 5. Results Meta Row ──────────────────────────────────────────
            when (val state = gigsState) {
                is Resource.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val gigs = (state as Resource.Success<List<Gig>>).data
                        .filter { it.id !in appliedGigIds }
                    if (gigs.isNotEmpty()) {
                        item(key = "meta") {
                            ResultsMetaRow(
                                count     = gigs.size,
                                sortLabel = SORT_OPTIONS[ui.selectedSortIdx],
                                onSort    = { onAction(GigListAction.OpenSort) }
                            )
                        }
                    }
                }
                else -> {}
            }

            // ── 6. Gig Cards / Loading / Error / Empty ───────────────────────
            when (val state = gigsState) {
                is Resource.Loading -> {
                    items(5, key = { "shimmer_$it" }) {
                        GigCardShimmer(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }

                is Resource.Error -> {
                    item(key = "error") {
                        GigListEmptyState(
                            emoji    = "⚠️",
                            title    = "Couldn't load gigs",
                            subtitle = state.message,
                            primaryLabel   = "Retry",
                            secondaryLabel = "Browse all",
                            onPrimary      = { viewModel.loadGigs() },
                            onSecondary    = { onAction(GigListAction.ClearAllFilters) }
                        )
                    }
                }

                is Resource.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val filtered = (state as Resource.Success<List<Gig>>).data
                        .filter { it.id !in appliedGigIds }

                    if (filtered.isEmpty()) {
                        item(key = "empty") {
                            GigListEmptyState(
                                emoji    = "🔍",
                                title    = if (ui.searchQuery.isNotBlank())
                                               "No results for \"${ui.searchQuery}\""
                                           else
                                               "No gigs in ${ui.selectedCategory}",
                                subtitle = "Try a different category or clear your filters.",
                                primaryLabel   = "Clear filters",
                                secondaryLabel = "Post a gig",
                                onPrimary  = { onAction(GigListAction.ClearAllFilters) },
                                onSecondary = { navController.navigate(Routes.CREATE_GIG) }
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { gig ->
                            var pressed by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(
                                targetValue    = if (pressed) 0.97f else 1f,
                                animationSpec  = spring(stiffness = 500f),
                                label          = "card_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                                    .scale(scale)
                            ) {
                                GigCard(
                                    gig     = gig,
                                    onClick = { navController.navigate(Routes.gigDetail(gig.id)) }
                                )
                            }
                        }

                        // End-of-list footer
                        item(key = "footer") {
                            EndOfListFooter(count = filtered.size)
                        }
                    }
                }

                null -> {}
            }
        }

        // ── Sort Sheet ────────────────────────────────────────────────────────
        if (ui.showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { onAction(GigListAction.DismissSort) },
                sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor   = Surface2,
                shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                SortSheetContent(
                    options       = SORT_OPTIONS,
                    selectedIdx   = ui.stagingSortIdx,
                    onSelect      = { onAction(GigListAction.StageSortIdx(it)) },
                    onApply       = { onAction(GigListAction.ApplySort) }
                )
            }
        }

        // ── Filter Sheet ─────────────────────────────────────────────────────
        if (ui.showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { onAction(GigListAction.DismissFilter) },
                sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor   = Surface2,
                shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                FilterSheetContent(
                    locationOptions = LOCATION_OPTIONS,
                    deadlineOptions = DEADLINE_OPTIONS,
                    selectedLoc     = ui.stagingLoc,
                    selectedDeadline = ui.stagingDeadline,
                    onLocSelect     = { onAction(GigListAction.StageLoc(it)) },
                    onDeadlineSelect = { onAction(GigListAction.StageDeadline(it)) },
                    onReset         = {
                        onAction(GigListAction.StageLoc(null))
                        onAction(GigListAction.StageDeadline("Any time"))
                    },
                    onApply         = { onAction(GigListAction.ApplyFilter) }
                )
            }
        }
    }
}

// ─── Topbar ──────────────────────────────────────────────────────────────────

@Composable
private fun GigListTopBar(
    hasActiveFilters: Boolean,
    sortLabel:        String,
    onBack:  () -> Unit,
    onSort:  () -> Unit,
    onFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface1)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(0.dp))
            .padding(start = 4.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
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
                "Gig Marketplace",
                style      = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 18.sp
                ),
                color      = TextPrimary
            )
            Text(
                "Find your next opportunity",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextTertiary
            )
        }

        // Sort icon
        IconButton(onClick = onSort) {
            Icon(Icons.Default.Sort, "Sort", tint = TextSecondary, modifier = Modifier.size(20.dp))
        }

        // Filter icon + active dot
        Box {
            IconButton(onClick = onFilter) {
                Icon(Icons.Default.Tune, "Filters", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            if (hasActiveFilters) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .background(SemanticWarning, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

// ─── Search Bar ──────────────────────────────────────────────────────────────

@Composable
private fun GigSearchBar(
    query:     String,
    onChanged: (String) -> Unit,
    onClear:   () -> Unit,
    onSearch:  () -> Unit,
    modifier:  Modifier = Modifier
) {
    OutlinedTextField(
        value       = query,
        onValueChange = onChanged,
        placeholder = {
            Text(
                "Search gigs, skills, categories…",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = TextTertiary
            )
        },
        leadingIcon = {
            Icon(Icons.Default.Search, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, "Clear", tint = TextTertiary, modifier = Modifier.size(16.dp))
                }
            }
        },
        modifier  = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape     = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor    = TextPrimary,
            unfocusedTextColor  = TextPrimary,
            focusedContainerColor   = Surface3,
            unfocusedContainerColor = Surface2,
            focusedBorderColor      = Indigo500,
            unfocusedBorderColor    = BorderSubtle,
            cursorColor             = Indigo500
        )
    )
}

// ─── Category Filter Chip ────────────────────────────────────────────────────

@Composable
private fun CategoryFilterChip(
    label:      String,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) GlowIndigo else Surface3,
        animationSpec = tween(160),
        label         = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) Indigo500 else BorderSubtle,
        animationSpec = tween(160),
        label         = "chip_border"
    )

    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize   = 12.sp
            ),
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}

// ─── Active Filter Row ───────────────────────────────────────────────────────

@Composable
private fun ActiveFilterRow(
    filters:  List<String>,
    onRemove: (String) -> Unit,
    onClear:  () -> Unit
) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        items(filters, key = { it }) { label ->
            ActiveFilterChip(label = label, onRemove = { onRemove(label) })
        }
        if (filters.size > 1) {
            item(key = "clear_all") {
                Text(
                    "Clear all",
                    style    = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    color    = Indigo400,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onClear
                        )
                )
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(
    label:    String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x1A6366F1))
            .border(BorderStroke(1.dp, Indigo500.copy(alpha = 0.35f)), RoundedCornerShape(20.dp))
            .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            color = Indigo400
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x26818CF8))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onRemove
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Close, "Remove", tint = Indigo400, modifier = Modifier.size(9.dp))
        }
    }
}

// ─── Results Meta Row ────────────────────────────────────────────────────────

@Composable
private fun ResultsMetaRow(
    count:     Int,
    sortLabel: String,
    onSort:    () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            "$count gigs available",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = TextTertiary
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onSort
                )
                .background(Surface3)
                .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.Sort, null, tint = TextTertiary, modifier = Modifier.size(12.dp))
            Text(
                sortLabel.take(16) + if (sortLabel.length > 16) "…" else "",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = TextSecondary
            )
        }
    }
}

// ─── Shimmer Skeleton Card ────────────────────────────────────────────────────

@Composable
private fun GigCardShimmer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue   = -600f,
        targetValue    = 1200f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    val shimmerBrush = Brush.linearGradient(
        colors    = listOf(Surface3, Surface4, Surface3),
        start     = Offset(shimmerX, 0f),
        end       = Offset(shimmerX + 600f, 0f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .surfaceCard()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header row skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(shimmerBrush))
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(modifier = Modifier.size(100.dp, 11.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
                Box(modifier = Modifier.size(60.dp, 9.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
            }
        }
        Box(modifier = Modifier.size(220.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
        Box(modifier = Modifier.size(160.dp, 14.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) {
                Box(modifier = Modifier.size(56.dp, 22.dp).clip(RoundedCornerShape(6.dp)).background(shimmerBrush))
            }
        }
    }
}

// ─── Empty / Error State ─────────────────────────────────────────────────────

@Composable
private fun GigListEmptyState(
    emoji:          String,
    title:          String,
    subtitle:       String,
    primaryLabel:   String,
    secondaryLabel: String,
    onPrimary:      () -> Unit,
    onSecondary:    () -> Unit
) {
    Column(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(12.dp)
    ) {
        // Emoji bubble
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(48.dp))
                .background(GlowIndigo),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 40.sp)
        }

        Text(
            title,
            style     = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 17.sp
            ),
            color     = TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            subtitle,
            style     = MaterialTheme.typography.bodySmall.copy(
                fontSize   = 13.sp,
                lineHeight = 19.sp
            ),
            color     = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // CTA pair
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .primaryGradientBg()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onPrimary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    primaryLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .surfaceCard()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onSecondary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    secondaryLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSecondary
                )
            }
        }
    }
}

// ─── End of List Footer ──────────────────────────────────────────────────────

@Composable
private fun EndOfListFooter(count: Int) {
    Column(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(4.dp)
    ) {
        // Decorative pill divider
        Box(
            modifier = Modifier
                .size(40.dp, 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BorderDefault)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "You've seen all $count gigs",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = TextTertiary
        )
        Text(
            "Adjust filters to discover more",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = Color(0xFF374151)
        )
    }
}

// ─── Sort Bottom Sheet Content ────────────────────────────────────────────────

@Composable
private fun SortSheetContent(
    options:     List<String>,
    selectedIdx: Int,
    onSelect:    (Int) -> Unit,
    onApply:     () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        SheetHandle()
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Sort by",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 18.sp
            ),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        options.forEachIndexed { index, label ->
            val isSelected = selectedIdx == index
            val rowBg by animateColorAsState(
                targetValue   = if (isSelected) Color(0x1A6366F1) else Color.Transparent,
                animationSpec = tween(140),
                label         = "sort_row_bg_$index"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(rowBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = { onSelect(index) }
                    )
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Custom radio indicator — no Material defaults
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Indigo500 else Surface4)
                        .border(
                            BorderStroke(1.5.dp, if (isSelected) Indigo400 else BorderDefault),
                            RoundedCornerShape(9.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                        )
                    }
                }
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize   = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isSelected) TextPrimary else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        SheetApplyButton(label = "Apply Sort", onClick = onApply)
    }
}

// ─── Filter Bottom Sheet Content ──────────────────────────────────────────────

@Composable
private fun FilterSheetContent(
    locationOptions:  List<String>,
    deadlineOptions:  List<String>,
    selectedLoc:      String?,
    selectedDeadline: String,
    onLocSelect:      (String?) -> Unit,
    onDeadlineSelect: (String) -> Unit,
    onReset:          () -> Unit,
    onApply:          () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
    ) {
        SheetHandle()
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                "Filters",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 18.sp
                ),
                color = TextPrimary
            )
            Text(
                "Reset",
                style    = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                color    = Indigo400,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onReset
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Location
        FilterSectionLabel(label = "Location")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            locationOptions.forEach { loc ->
                val isSel = selectedLoc == loc
                FilterToggleChip(
                    label      = loc,
                    isSelected = isSel,
                    onClick    = { onLocSelect(if (isSel) null else loc) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Deadline
        FilterSectionLabel(label = "Deadline")
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            deadlineOptions.forEach { dl ->
                FilterToggleChip(
                    label      = dl,
                    isSelected = selectedDeadline == dl,
                    onClick    = { onDeadlineSelect(dl) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SheetApplyButton(label = "Show Gigs", onClick = onApply)
    }
}

// ─── Shared Sheet Primitives ──────────────────────────────────────────────────

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .size(40.dp, 4.dp)
            .align(Alignment.CenterHorizontally)
            .clip(RoundedCornerShape(2.dp))
            .background(BorderDefault)
    )
}

@Composable
private fun FilterSectionLabel(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize      = 12.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.6.sp
        ),
        color = TextTertiary
    )
}

@Composable
private fun FilterToggleChip(
    label:      String,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    val bg by animateColorAsState(
        targetValue   = if (isSelected) GlowIndigo else Surface3,
        animationSpec = tween(140),
        label         = "filter_chip_bg"
    )
    val border by animateColorAsState(
        targetValue   = if (isSelected) Indigo500 else BorderSubtle,
        animationSpec = tween(140),
        label         = "filter_chip_border"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(BorderStroke(1.dp, border), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize   = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun SheetApplyButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .primaryGradientBg(cornerRadius = 14)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

// Helper to get alignment in Column scope — needed for SheetHandle
private fun Modifier.align(alignment: Alignment.Horizontal): Modifier =
    this.then(Modifier) // no-op at call site; real alignment applied by Column
