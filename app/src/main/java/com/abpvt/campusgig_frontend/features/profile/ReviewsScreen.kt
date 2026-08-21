/**
 * ReviewsScreen.kt — Real reviews fetched from the backend API
 *
 * CONCEPT: The rating distribution chart makes trust assessment instant.
 *
 * FEATURES:
 * - Summary card: live rating + stars + distribution bar chart (5★→1★) computed from real data
 * - Filter chips: All / 5★ / 4★ / 3★ / 2★ / 1★
 * - Review cards: avatar + name + stars + timestamp + review text
 * - Empty state when no reviews exist
 */
package com.abpvt.campusgig_frontend.features.profile

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Review
import com.abpvt.campusgig_frontend.ui.theme.GradientGoldEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientGoldStart
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── ViewModel ────────────────────────────────────────────────────────────────

class ReviewsViewModel(private val api: ApiService) : ViewModel() {
    private val _reviews = MutableStateFlow<Resource<List<Review>>>(Resource.Loading)
    val reviews: StateFlow<Resource<List<Review>>> = _reviews.asStateFlow()

    fun loadReviews(userId: String) {
        viewModelScope.launch {
            _reviews.value = Resource.Loading
            _reviews.value = try {
                val response = api.getReviewsForUser(userId)
                if (response.isSuccessful) {
                    Resource.Success(response.body() ?: emptyList())
                } else {
                    Resource.Error("Failed to load reviews (${response.code()})")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Could not load reviews")
            }
        }
    }
}

class ReviewsViewModelFactory(private val api: ApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ReviewsViewModel(api) as T
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ReviewsScreen(
    navController: NavController,
    userId: String
) {
    val context = LocalContext.current
    val api = (context.applicationContext as CampusGigApplication).api
    val viewModel: ReviewsViewModel = viewModel(factory = ReviewsViewModelFactory(api))

    LaunchedEffect(userId) { viewModel.loadReviews(userId) }

    val reviewsState by viewModel.reviews.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val state = reviewsState) {
            is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
            is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😕", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text("Retry", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.loadReviews(userId) })
                }
            }
            is Resource.Success -> ReviewsContent(
                reviews = state.data,
                navController = navController
            )
            null -> {}
        }
    }
}

@Composable
private fun ReviewsContent(reviews: List<Review>, navController: NavController) {
    var selectedFilter by remember { mutableIntStateOf(0) }

    // Compute stats from real data
    val totalReviews = reviews.size
    val avgRating = if (totalReviews > 0) reviews.sumOf { it.rating } / totalReviews.toDouble() else 0.0
    val ratingDistribution = (1..5).associateWith { star -> reviews.count { it.rating == star } }

    val filteredReviews = when (selectedFilter) {
        0 -> reviews
        else -> reviews.filter { it.rating == (6 - selectedFilter) }
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 40.dp)) {

        // ── Header ────────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Reviews", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        if (totalReviews == 0) "No reviews yet" else "from $totalReviews verified gigs",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // ── Summary Card ──────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GradientIndigoStart.copy(alpha = 0.06f))
                    .border(1.dp, Brush.linearGradient(listOf(GradientIndigoStart.copy(0.4f), GradientIndigoEnd.copy(0.2f))), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                if (totalReviews == 0) {
                    // Empty state
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⭐", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No reviews yet", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                        Text("Reviews appear after completing gigs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Left: Big rating
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
                            Text(
                                "%.1f".format(avgRating),
                                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Row {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < avgRating.toInt()) SemanticWarning else SemanticWarning.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$totalReviews reviews", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                        Box(modifier = Modifier.size(1.dp, 100.dp).background(MaterialTheme.colorScheme.outline))
                        Spacer(modifier = Modifier.width(16.dp))

                        // Right: Distribution bars
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                            (5 downTo 1).forEach { star ->
                                val count = ratingDistribution[star] ?: 0
                                val fraction = if (totalReviews > 0) count.toFloat() / totalReviews else 0f
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$star★", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.width(22.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction.coerceAtLeast(if (count > 0) 0.05f else 0f))
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Brush.horizontalGradient(colors = listOf(GradientGoldStart, GradientGoldEnd)))
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("$count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.width(18.dp))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Filter Chips ──────────────────────────────────────────────────────
        if (totalReviews > 0) {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "5★", "4★", "3★", "2★", "1★").forEachIndexed { index, label ->
                        val isSelected = selectedFilter == index
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                                    else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                )
                                .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { selectedFilter = index }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── Review Cards ──────────────────────────────────────────────────────
        items(filteredReviews) { review ->
            ReviewCard(review = review, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (totalReviews > 0 && filteredReviews.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⭐", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No ${6 - selectedFilter}★ reviews yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: Review, modifier: Modifier = Modifier) {
    val reviewerName = review.reviewer?.name ?: "Anonymous"
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                // Reviewer avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GradientIndigoStart.copy(alpha = 0.2f), GradientIndigoEnd.copy(alpha = 0.2f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        reviewerName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(reviewerName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i < review.rating) SemanticWarning else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
                // Relative time (simple display of createdAt if available)
                if (review.createdAt.isNotBlank()) {
                    Text(
                        formatDate(review.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            if (review.comment.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(review.comment, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Simple date formatter: shows month + year from ISO string */
private fun formatDate(iso: String): String {
    return try {
        val parts = iso.substring(0, 10).split("-")
        val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val month = months.getOrNull(parts[1].toIntOrNull() ?: 0) ?: ""
        "$month ${parts[0]}"
    } catch (e: Exception) {
        ""
    }
}
