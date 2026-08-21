/**
 * GigApplicationsScreen.kt — v3.0 — Premium Employer Applicant Review Dashboard
 *
 * CONCEPT: A decisive, high-signal review screen. The employer can see every
 * applicant's proposal, bid, and skills at a glance, and act with one tap.
 *
 * FEATURES:
 * - v3.0 styled header (MaterialTheme.colorScheme.surfaceVariant icon btn, gradient bottom line)
 * - Applications count summary banner with teal accent
 * - ApplicantRowCard: glassmorphic dark card with indigo left accent line
 * - Avatar with gradient ring
 * - Proposal text box with indigo left-border accent
 * - Budget displayed as a SemanticSuccess pill badge
 * - Pending actions: red ghost Reject + gradient-filled Accept
 * - Accepted state: gradient "Chat with Candidate" button
 * - Staggered card fade-in animation
 * - Premium empty state with gradient ring
 */
package com.abpvt.campusgig_frontend.features.gigs

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ApplicationViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Application
import com.abpvt.campusgig_frontend.features.applications.ApplicationViewModel
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.components.AvatarInitials
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.GradientTealEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientTealStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.ui.theme.Violet400

@Composable
fun GigApplicationsScreen(
    navController: NavController,
    gigId: String,
    viewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).apiService
        )
    )
) {
    val context = LocalContext.current

    LaunchedEffect(gigId) {
        viewModel.loadApplicationsForGig(gigId)
    }

    val appsState by viewModel.gigApplications.collectAsState()
    val isActionInProgress by viewModel.actionInProgress.collectAsState()
    val actionError by viewModel.actionError.collectAsState()

    LaunchedEffect(actionError) {
        actionError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearActionError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── v3.0 Header ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Gradient bottom border line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    GradientIndigoStart.copy(alpha = 0.4f),
                                    GradientTealStart.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Applications Received",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Review candidates for your gig",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            when (val state = appsState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.5.dp
                        )
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("😕", fontSize = 36.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                state.message,
                                color = SemanticError,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                is Resource.Success -> {
                    val applications = state.data
                    if (applications.isEmpty()) {
                        // ── Premium Empty State ───────────────────────────────
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                // Gradient ring container
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    GradientTealStart.copy(alpha = 0.5f),
                                                    GradientIndigoStart.copy(alpha = 0.4f)
                                                )
                                            )
                                        )
                                        .padding(3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.background),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📁", fontSize = 36.sp)
                                    }
                                }
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    "No applications yet",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Keep sharing your gig to attract the right candidates.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // ── Applications count summary banner ─────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            GradientTealStart.copy(alpha = 0.08f),
                                            GradientIndigoStart.copy(alpha = 0.06f)
                                        )
                                    )
                                )
                                .border(
                                    width = 0.dp,
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(GradientTealStart)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "${applications.size} candidate${if (applications.size == 1) "" else "s"} applied",
                                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                // Pending count chip
                                val pendingCount = applications.count { it.status.lowercase() == "pending" }
                                if (pendingCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(SemanticWarning.copy(alpha = 0.12f))
                                            .border(1.dp, SemanticWarning.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            "$pendingCount pending",
                                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SemanticWarning)
                                        )
                                    }
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(applications, key = { _, it -> it.id }) { index, application ->
                                // Staggered entry fade-in
                                var visible by remember { mutableStateOf(false) }
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(index * 60L)
                                    visible = true
                                }
                                val alpha by animateFloatAsState(
                                    targetValue = if (visible) 1f else 0f,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "cardAlpha_$index"
                                )
                                Box(modifier = Modifier.alpha(alpha)) {
                                    ApplicantRowCard(
                                        application = application,
                                        isLoading = isActionInProgress,
                                        onStatusUpdate = { newStatus ->
                                            viewModel.updateApplicationStatus(application.id, newStatus) { success ->
                                                if (success) {
                                                    if (newStatus == "accepted") {
                                                        val candidateName = application.applicant?.name ?: "Candidate"
                                                        val candidateId   = application.applicant?.id ?: ""
                                                        if (candidateId.isNotBlank()) {
                                                            navController.navigate(
                                                                Routes.chat(
                                                                    receiverId   = candidateId,
                                                                    receiverName = candidateName,
                                                                    gigTitle     = application.gig?.title ?: "Gig Discussion",
                                                                    gigBudget    = "₹${application.expectedBudget.toInt()}"
                                                                )
                                                            )
                                                        }
                                                    } else {
                                                        viewModel.loadApplicationsForGig(gigId)
                                                    }
                                                }
                                            }
                                        },
                                        onChatClick = {
                                            val name = application.applicant?.name ?: "Candidate"
                                            val id   = application.applicant?.id ?: ""
                                            if (id.isNotBlank()) {
                                                navController.navigate(
                                                    Routes.chat(
                                                        receiverId   = id,
                                                        receiverName = name,
                                                        gigTitle     = application.gig?.title,
                                                        gigBudget    = "₹${application.expectedBudget.toInt()}"
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Full-screen loading overlay during accept/reject action
        if (isActionInProgress) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Processing...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}

// ─── Applicant Row Card ────────────────────────────────────────────────────────
@Composable
private fun ApplicantRowCard(
    application: Application,
    isLoading: Boolean,
    onStatusUpdate: (String) -> Unit,
    onChatClick: () -> Unit
) {
    val applicant = application.applicant
    val status    = application.status.lowercase()

    // Card with glassmorphic style + indigo left accent line
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        GradientIndigoStart.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // ── Indigo left accent line ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(210.dp) // approximate, will flex
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                when (status) {
                                    "accepted" -> SemanticSuccess
                                    "rejected" -> SemanticError
                                    else       -> GradientIndigoStart
                                },
                                when (status) {
                                    "accepted" -> SemanticSuccess.copy(alpha = 0.3f)
                                    "rejected" -> SemanticError.copy(alpha = 0.3f)
                                    else       -> Violet400.copy(alpha = 0.4f)
                                }
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                // ── Applicant header row ──────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar with gradient ring
                    Box {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(GradientIndigoStart, GradientTealStart)
                                    )
                                )
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                AvatarInitials(name = applicant?.name ?: "C", size = 40)
                            }
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = applicant?.name ?: "Candidate",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = applicant?.college ?: "College",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    // Status chip (non-pending)
                    if (status != "pending") {
                        val chipColor = when (status) {
                            "accepted" -> SemanticSuccess
                            "rejected" -> SemanticError
                            else       -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(chipColor.copy(alpha = 0.12f))
                                .border(1.dp, chipColor.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = status.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = chipColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Proposal text with indigo left-border accent ──────────────
                Text(
                    text = "PROPOSAL",
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Indigo left border that matches the height of the text box
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.verticalGradient(
                                    listOf(MaterialTheme.colorScheme.primary, Violet400.copy(alpha = 0.3f))
                                )
                            )
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = application.proposal,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 19.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Budget pill + action buttons ──────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Budget badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SemanticSuccess.copy(alpha = 0.1f))
                            .border(1.dp, SemanticSuccess.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "₹${application.expectedBudget.toInt()} bid",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = SemanticSuccess
                        )
                    }

                    // Action section
                    when (status) {
                        "pending" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Reject — red ghost button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SemanticError.copy(alpha = 0.08f))
                                        .border(1.dp, SemanticError.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .clickable(enabled = !isLoading) { onStatusUpdate("rejected") }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "Reject",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = SemanticError
                                    )
                                }
                                // Accept — gradient filled button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(GradientIndigoStart, GradientIndigoEnd)
                                            )
                                        )
                                        .clickable(enabled = !isLoading) { onStatusUpdate("accepted") }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        "Accept",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        "accepted" -> {
                            // Chat with candidate — gradient button with send icon
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(GradientIndigoStart, GradientTealStart)
                                        )
                                    )
                                    .clickable { onChatClick() }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Chat",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
