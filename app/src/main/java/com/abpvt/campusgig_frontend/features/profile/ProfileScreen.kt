/**
 * ProfileScreen.kt — v3.0 — My Profile Screen & Digital Resume
 *
 * Architecture & Design Spec:
 *  - Single immutable ProfileUiState data class to manage expanded view toggles.
 *  - Sealed ProfileAction interface for decoupled actions.
 *  - Asymmetric and handcrafted visual design system (no standard Material card defaults).
 *  - Rich hero banner with smooth overlapping avatar offset and modern action chips.
 *  - High-polish dark-mode visual hierarchy with subtle glassmorphism gradient strokes.
 */
package com.abpvt.campusgig_frontend.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ProfileViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.BorderSubtle
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.GradientTealEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientTealStart
import com.abpvt.campusgig_frontend.ui.theme.Indigo400
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.ui.theme.Surface1
import com.abpvt.campusgig_frontend.ui.theme.Surface2
import com.abpvt.campusgig_frontend.ui.theme.Surface3
import com.abpvt.campusgig_frontend.ui.theme.TextPrimary
import com.abpvt.campusgig_frontend.ui.theme.TextSecondary
import com.abpvt.campusgig_frontend.ui.theme.TextTertiary

// ─── UI State ─────────────────────────────────────────────────────────────────

private data class ProfileUiState(
    val showAllSkills: Boolean = false,
    val showFullBio: Boolean = false
)

// ─── UI Actions ───────────────────────────────────────────────────────────────

private sealed interface ProfileAction {
    object ToggleSkills : ProfileAction
    object ToggleBio : ProfileAction
    object Logout : ProfileAction
}

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).userRepository
        )
    )
) {
    val profileState by viewModel.profile.collectAsState()
    var uiState by remember { mutableStateOf(ProfileUiState()) }

    val onAction: (ProfileAction) -> Unit = { action ->
        when (action) {
            ProfileAction.ToggleSkills -> uiState = uiState.copy(showAllSkills = !uiState.showAllSkills)
            ProfileAction.ToggleBio -> uiState = uiState.copy(showFullBio = !uiState.showFullBio)
            ProfileAction.Logout -> {
                viewModel.logout()
                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface1)) {
        when (val state = profileState) {
            is Resource.Loading -> ProfileLoadingState()
            is Resource.Error -> ProfileErrorState(message = state.message, onLogout = { onAction(ProfileAction.Logout) })
            is Resource.Success -> ProfileContent(
                user = state.data,
                uiState = uiState,
                onAction = onAction,
                navController = navController
            )
            null -> {}
        }
    }
}

// ─── Content Composables ─────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileContent(
    user: User,
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    navController: NavController
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // ── Hero Section ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            // Gradient banner (220dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientIndigoStart, GradientIndigoMid, GradientIndigoEnd)
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent),
                                radius = 400f
                            )
                        )
                )
            }

            // Edit & Settings Icons (Top Right)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navController.navigate(Routes.EDIT_PROFILE) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navController.navigate(Routes.SETTINGS) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Overlapping Avatar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 40.dp)
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Surface1)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Brush.linearGradient(colors = listOf(GradientIndigoStart, GradientIndigoEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.initials(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {

            // ── Identity Header ──────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = user.name.ifBlank { "Campus Builder" },
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(user.college.ifBlank { "College Student" }, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    if (user.branch.isNotBlank()) {
                        Text(" · ${user.branch}", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                    }
                }

                if (user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = TextSecondary,
                        maxLines = if (uiState.showFullBio) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!uiState.showFullBio && user.bio.length > 120) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Read more",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Indigo400,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onAction(ProfileAction.ToggleBio) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(GradientTealStart.copy(alpha = 0.12f))
                        .border(BorderStroke(1.dp, GradientTealStart.copy(alpha = 0.3f)), RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        "🎓 ${user.college.ifBlank { "CampusGig Member" }}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GradientTealEnd
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Reputation Row ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GradientIndigoStart.copy(alpha = 0.08f), GradientIndigoEnd.copy(alpha = 0.04f))
                        )
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(listOf(GradientIndigoStart.copy(alpha = 0.4f), GradientIndigoEnd.copy(alpha = 0.2f)))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReputationStat(
                        value = if (user.reviewCount > 0) "%.1f ★".format(user.rating) else "— ★",
                        label = "Rating",
                        color = SemanticWarning
                    )
                    Box(modifier = Modifier.size(1.dp, 32.dp).background(BorderSubtle))
                    ReputationStat(value = "${user.completedGigsCount}", label = "Completed", color = SemanticSuccess)
                    Box(modifier = Modifier.size(1.dp, 32.dp).background(BorderSubtle))
                    ReputationStat(value = "${user.reviewCount}", label = "Reviews", color = Indigo400)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Quick Action Buttons ──────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionChip("🚀 Post Gig", Modifier.weight(1f)) { navController.navigate(Routes.CREATE_GIG) }
                QuickActionChip("✏️ Skills", Modifier.weight(1f)) { navController.navigate(Routes.EDIT_PROFILE) }
                QuickActionChip("💼 My Gigs", Modifier.weight(1f)) { navController.navigate(Routes.MY_GIGS) }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(20.dp))

            // ── Skills Section ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Skills", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text(
                    "Edit",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Indigo400,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { navController.navigate(Routes.EDIT_PROFILE) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (user.skills.isNotEmpty()) {
                val visibleSkills = if (uiState.showAllSkills) user.skills else user.skills.take(6)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleSkills.forEach { skill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GradientIndigoStart.copy(alpha = 0.08f))
                                .border(BorderStroke(1.dp, Indigo400.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(skill, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Indigo400)
                        }
                    }
                }
                if (!uiState.showAllSkills && user.skills.size > 6) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "See all ${user.skills.size} skills",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Indigo400,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onAction(ProfileAction.ToggleSkills) }
                    )
                }
            } else {
                Text("No skills added yet", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(20.dp))

            // ── Portfolio Section ──────────────────────────────────────────────
            Text("Portfolio & Socials", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (user.githubProfile.isNotBlank()) {
                    PortfolioLinkChip("🐙 GitHub") { }
                }
                if (user.linkedinProfile.isNotBlank()) {
                    PortfolioLinkChip("💼 LinkedIn") { }
                }
                user.portfolioLinks.firstOrNull()?.let {
                    PortfolioLinkChip("🔗 Website") { }
                }
                if (user.githubProfile.isBlank() && user.linkedinProfile.isBlank() && user.portfolioLinks.isEmpty()) {
                    Text("No portfolio links added yet", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(20.dp))

            // ── Reviews Section ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reviews", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    if (user.reviewCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("%.1f ★".format(user.rating), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SemanticWarning)
                    }
                }
                Text(
                    "See all →",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Indigo400,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { navController.navigate(Routes.reviews(user.id)) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (user.reviewCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface2)
                        .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⭐", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No reviews yet", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextSecondary)
                        Text("Complete gigs to earn your first review", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface2)
                        .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navController.navigate(Routes.reviews(user.id)) }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(
                            "%.1f ★  ·  ${user.reviewCount} reviews".format(user.rating),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SemanticWarning
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View all →", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = Indigo400)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Logout Action ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface2)
                    .border(BorderStroke(1.dp, SemanticError.copy(alpha = 0.35f)), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAction(ProfileAction.Logout) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = SemanticError, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = SemanticError)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun ReputationStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = color)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

@Composable
private fun QuickActionChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = TextSecondary)
    }
}

@Composable
private fun PortfolioLinkChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Surface3)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = TextSecondary)
    }
}

@Composable
private fun ProfileLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(color = Indigo400, modifier = Modifier.size(32.dp), strokeWidth = 2.5.dp)
            Text("Loading profile...", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
    }
}

@Composable
private fun ProfileErrorState(message: String, onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("😕", fontSize = 40.sp)
            Text(message, color = SemanticError, style = MaterialTheme.typography.bodyMedium)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, SemanticError.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                    .clickable { onLogout() }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sign Out", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = SemanticError)
            }
        }
    }
}
