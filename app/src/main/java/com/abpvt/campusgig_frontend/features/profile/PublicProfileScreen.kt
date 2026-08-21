/**
 * PublicProfileScreen.kt — v3.0 — Premium Read-Only Public Profile
 *
 * CONCEPT: When you click someone's avatar in chat or a gig applicant card,
 * this screen gives you an iInstagram / LinkedIn-level polish profile view.
 *
 * FEATURES:
 * - Layered hero: diagonal gradient + radial glow blob + glassmorphic back button
 * - Avatar with glowing gradient ring
 * - Role badge chip (Freelancer / Employer)
 * - Glassmorphic reputation row (gradient border, stats)
 * - Gradient-border skill chips
 * - Portfolio icon-cards with teal accent
 * - Star rating preview strip
 * - Message gradient CTA with indigo glow
 */
package com.abpvt.campusgig_frontend.features.profile

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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.components.AvatarInitials
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.GradientTealEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientTealStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.ui.theme.Violet400
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── ViewModel ────────────────────────────────────────────────────────────────

class PublicProfileViewModel(private val api: ApiService) : ViewModel() {
    private val _user = MutableStateFlow<Resource<User>>(Resource.Loading)
    val user: StateFlow<Resource<User>> = _user.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _user.value = Resource.Loading
            _user.value = try {
                val response = api.getUserById(userId)
                if (response.isSuccessful) Resource.Success(response.body()!!)
                else Resource.Error("User not found (${response.code()})")
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}

class PublicProfileViewModelFactory(private val api: ApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PublicProfileViewModel(api) as T
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicProfileScreen(
    navController: NavController,
    userId: String
) {
    val context = LocalContext.current
    val api = (context.applicationContext as CampusGigApplication).api
    val viewModel: PublicProfileViewModel = viewModel(factory = PublicProfileViewModelFactory(api))

    LaunchedEffect(userId) { viewModel.loadUser(userId) }

    val userState by viewModel.user.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val state = userState) {
            is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.5.dp
                )
            }
            is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😕", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
                    }
                }
            }
            is Resource.Success -> PublicProfileContent(
                user = state.data,
                navController = navController,
                userId = userId
            )
            null -> {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PublicProfileContent(user: User, navController: NavController, userId: String) {
    var showFullBio by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // ── Hero ──────────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {

            // Layer 1: diagonal gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0D1228),
                                GradientIndigoStart.copy(alpha = 0.7f),
                                GradientTealStart.copy(alpha = 0.5f)
                            )
                        )
                    )
            )

            // Layer 2: radial glow blob behind avatar area
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-20).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GradientIndigoMid.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Layer 3: subtle dot pattern via nested transparent circles
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GradientTealStart.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            radius = 400f
                        )
                    )
            )

            // Back + More buttons (glassmorphic)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 16.dp, vertical = 52.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Avatar with glowing gradient ring
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 44.dp)
            ) {
                // Outer glow ring (gradient)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(GradientIndigoStart, GradientTealStart, Violet400)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner padding ring (MaterialTheme.colorScheme.background spacer)
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        // Avatar
                        AvatarInitials(name = user.name.ifBlank { "U" }, size = 82)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(52.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            // ── Identity ──────────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    user.name.ifBlank { "Campus User" },
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.college.ifBlank { "College" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (user.branch.isNotBlank()) {
                        Text(" · ${user.branch}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Role badge + College badge row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Role chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GradientIndigoStart.copy(alpha = 0.15f), GradientIndigoEnd.copy(alpha = 0.1f))
                                )
                            )
                            .border(
                                1.dp,
                                Brush.horizontalGradient(listOf(GradientIndigoStart.copy(0.4f), GradientIndigoEnd.copy(0.2f))),
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (user.skills.isNotEmpty()) "⚡ Freelancer" else "🏢 Employer",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    // College badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(GradientTealStart.copy(alpha = 0.1f))
                            .border(1.dp, GradientTealStart.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            "🎓 ${user.college.ifBlank { "College" }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GradientTealEnd
                        )
                    }
                }

                if (user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (showFullBio) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    if (!showFullBio && user.bio.length > 120) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Read more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showFullBio = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Message CTA ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoMid, GradientIndigoEnd))
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(GradientIndigoEnd.copy(alpha = 0.5f), Violet400.copy(alpha = 0.3f))),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { navController.navigate(Routes.chat(userId, user.name)) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.Message,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Message ${user.name.split(" ").firstOrNull() ?: ""}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Glassmorphic Reputation Row ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GradientIndigoStart.copy(alpha = 0.06f))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                GradientIndigoStart.copy(0.35f),
                                GradientIndigoMid.copy(0.2f),
                                GradientIndigoEnd.copy(0.1f)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    // Rating
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (user.reviewCount > 0) "${"%.1f".format(user.rating)}" else "—",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = SemanticWarning
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = SemanticWarning.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text("Rating", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                    // Vertical divider
                    Box(modifier = Modifier.size(1.dp, 40.dp).background(MaterialTheme.colorScheme.outline))
                    // Completed
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${user.completedGigsCount}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                    // Vertical divider
                    Box(modifier = Modifier.size(1.dp, 40.dp).background(MaterialTheme.colorScheme.outline))
                    // Reviews
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${user.reviewCount}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = SemanticSuccess
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Reviews", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Section: Skills ─────────────────────────────────────────────────
            SectionHeader(title = "Skills")
            Spacer(modifier = Modifier.height(12.dp))
            if (user.skills.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    user.skills.forEach { skill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GradientIndigoStart.copy(alpha = 0.08f))
                                .border(
                                    1.dp,
                                    Brush.horizontalGradient(
                                        listOf(GradientIndigoStart.copy(0.35f), GradientIndigoEnd.copy(0.15f))
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                skill,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                Text("No skills added yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }

            Spacer(modifier = Modifier.height(28.dp))
            GradientDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // ── Section: Portfolio ──────────────────────────────────────────────
            SectionHeader(title = "Portfolio")
            Spacer(modifier = Modifier.height(12.dp))
            if (user.githubProfile.isNotBlank() || user.linkedinProfile.isNotBlank() || user.portfolioLinks.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (user.githubProfile.isNotBlank()) {
                        PortfolioChip(emoji = "🐙", label = "GitHub")
                    }
                    if (user.linkedinProfile.isNotBlank()) {
                        PortfolioChip(emoji = "💼", label = "LinkedIn")
                    }
                    user.portfolioLinks.firstOrNull()?.let {
                        PortfolioChip(emoji = "🔗", label = "Website")
                    }
                }
            } else {
                Text("No portfolio links added", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }

            Spacer(modifier = Modifier.height(28.dp))
            GradientDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // ── Section: Reviews ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader(title = "Reviews")
                    if (user.reviewCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${"%.1f".format(user.rating)} ★",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SemanticWarning
                        )
                    }
                }
                Text(
                    "See all →",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { navController.navigate(Routes.reviews(userId)) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Star rating visual strip
            if (user.reviewCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val fullStars = user.rating.toInt().coerceIn(0, 5)
                    repeat(5) { idx ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (idx < fullStars) SemanticWarning else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${"%.1f".format(user.rating)} out of 5 · ${user.reviewCount} review${if (user.reviewCount == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Text(
                    "No reviews yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ─── Reusable sub-components ──────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun GradientDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, MaterialTheme.colorScheme.outline, Color.Transparent)
                )
            )
    )
}

@Composable
private fun PortfolioChip(emoji: String, label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(GradientTealStart.copy(0.3f), MaterialTheme.colorScheme.outline)),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
