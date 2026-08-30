/**
 * GigDetailScreen.kt — v3.0 — Full Gig Detail View
 *
 * Architecture:
 *  - Immutable GigDetailUiState data class owns ALL transient UI state.
 *  - Sealed GigDetailAction interface; one onAction() lambda drives all mutations.
 *  - No state is managed inside sub-composable bodies — all hoisted to screen.
 *  - collapseProgress is derivedStateOf (never recomputes unless scrollState changes).
 *
 * Visual Spec (Stripe / Airbnb / Linear inspired):
 *  - 220dp → 56dp collapsing hero with category gradient.
 *  - Content card overlaps hero by 20dp; 24dp top-radius lift.
 *  - Gradient-border budget card (Indigo → Violet, 1dp stroke).
 *  - 5-line expandable description with animated height.
 *  - FlowRow skill chips with indigo glow border.
 *  - Overlapping avatar stack for application count.
 *  - Sticky bottom action bar with 6 CTA states.
 *  - Apply sheet: multi-field, real-time validation, submit spinner.
 *  - ApplicantCard: proposal preview, bid amount, Accept/Reject/Chat.
 */
package com.abpvt.campusgig_frontend.features.gigs

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ApplicationViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.GigViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.HomeViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Application
import com.abpvt.campusgig_frontend.data.model.Gig
import com.abpvt.campusgig_frontend.features.applications.ApplicationViewModel
import com.abpvt.campusgig_frontend.features.home.HomeViewModel
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.components.AvatarInitials
import com.abpvt.campusgig_frontend.ui.components.LeaveReviewDialog
import com.abpvt.campusgig_frontend.ui.components.SlideToActionButton
import com.abpvt.campusgig_frontend.ui.theme.GlowIndigo
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticErrorBg
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccessBg
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.ui.theme.Violet500
import kotlinx.coroutines.launch

// ─── Category Hero Gradient ───────────────────────────────────────────────────

private fun categoryGradient(category: String) = when (category.lowercase()) {
    "coding"      -> listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4338CA))
    "design"      -> listOf(Color(0xFF500724), Color(0xFF831843), Color(0xFFBE185D))
    "writing"     -> listOf(Color(0xFF042F2E), Color(0xFF134E4A), Color(0xFF0D9488))
    "tutoring"    -> listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFFD97706))
    "photography" -> listOf(Color(0xFF450A0A), Color(0xFF7F1D1D), Color(0xFFDC2626))
    "video"       -> listOf(Color(0xFF0C1C3E), Color(0xFF1E3A5F), Color(0xFF1D4ED8))
    "marketing"   -> listOf(Color(0xFF2E1065), Color(0xFF4C1D95), Color(0xFF7C3AED))
    else          -> listOf(Color(0xFF0F0C29), Color(0xFF1E1B4B), Color(0xFF3730A3))
}

private fun categoryIcon(category: String) = when (category.lowercase()) {
    "coding"      -> "⌨️"
    "design"      -> "🎨"
    "writing"     -> "✍️"
    "tutoring"    -> "📚"
    "photography" -> "📷"
    "video"       -> "🎬"
    "marketing"   -> "📣"
    else          -> "💼"
}

// ─── UI State ─────────────────────────────────────────────────────────────────

private data class GigDetailUiState(
    val isSaved:             Boolean = false,
    val showApplySheet:      Boolean = false,
    val descExpanded:        Boolean = false,
    // Apply sheet fields
    val proposal:            String  = "",
    val expectedBudget:      String  = "",
    val proposalError:       String  = "",
    // Submit Work sheet fields (Worker)
    val showSubmitWorkSheet: Boolean = false,
    val submittedUrl:        String  = "",
    val submittedNote:       String  = "",
    val submittedUrlError:   String  = "",
    // Complete OTP sheet fields (Employer)
    val showOtpSheet:        Boolean = false,
    val otpInput:            String  = "",
    val otpError:            String  = "",
    val displayedOtp:        String  = ""
)

// ─── UI Actions ───────────────────────────────────────────────────────────────

private sealed interface GigDetailAction {
    object ToggleSave                                   : GigDetailAction
    object OpenApplySheet                               : GigDetailAction
    object DismissApplySheet                            : GigDetailAction
    object ToggleDescription                            : GigDetailAction
    data class ProposalChanged(val v: String)           : GigDetailAction
    data class BudgetChanged(val v: String)             : GigDetailAction
    data class Submit(
        val defaultBudget: Double,
        val onSubmit: (String, Double) -> Unit
    )                                                   : GigDetailAction

    // Work Submission Actions
    object OpenSubmitWorkSheet                          : GigDetailAction
    object DismissSubmitWorkSheet                       : GigDetailAction
    data class SubmittedUrlChanged(val v: String)       : GigDetailAction
    data class SubmittedNoteChanged(val v: String)      : GigDetailAction
    data class PerformSubmitWork(val onSubmit: (String, String) -> Unit) : GigDetailAction

    // OTP Completion Actions
    object OpenOtpSheet                                 : GigDetailAction
    object DismissOtpSheet                              : GigDetailAction
    data class OtpInputChanged(val v: String)           : GigDetailAction
    data class PerformCompleteGig(val onSubmit: (String) -> Unit) : GigDetailAction
    object RequestOtp                                   : GigDetailAction
}

// ─── Modifier Extensions ─────────────────────────────────────────────────────

@Composable
private fun Modifier.detailCard(radius: Int = 14): Modifier = this
    .clip(RoundedCornerShape(radius.dp))
    .background(MaterialTheme.colorScheme.surface)
    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(radius.dp))

private fun Modifier.indigoGradientBg(radius: Int = 14): Modifier = this
    .clip(RoundedCornerShape(radius.dp))
    .background(
        Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoMid, GradientIndigoEnd))
    )

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GigDetailScreen(
    navController: NavController,
    gigId: String,
    viewModel: GigViewModel = viewModel(
        factory = GigViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).gigRepository
        )
    )
) {
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).gigRepository,
            (LocalContext.current.applicationContext as CampusGigApplication).userRepository
        )
    )
    val appViewModel: ApplicationViewModel = viewModel(
        factory = ApplicationViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).apiService
        )
    )

    val gigState         by viewModel.selectedGig.collectAsState()
    val profileState     by homeViewModel.currentUser.collectAsState()
    val applyState       by appViewModel.applyState.collectAsState()
    val submitWorkState  by viewModel.submitWorkState.collectAsState()
    val otpState         by viewModel.otpState.collectAsState()
    val completeGigState by viewModel.completeGigState.collectAsState()

    val myAppsState      by appViewModel.applications.collectAsState()
    val currentUser     by remember { derivedStateOf { (profileState as? Resource.Success)?.data } }
    val gig             by remember { derivedStateOf { (gigState as? Resource.Success<Gig>)?.data } }
    val isOwner         by remember { derivedStateOf { gig?.employer?.id == currentUser?.id } }
    val isAcceptedWorker by remember {
        derivedStateOf {
            val uid = currentUser?.id ?: ""
            val acceptedByGig = gig?.acceptedApplicant != null && uid.isNotEmpty() && (
                gig?.acceptedApplicant.equals(uid, ignoreCase = true) ||
                gig?.acceptedApplicant?.contains(uid, ignoreCase = true) == true
            )
            val acceptedByApp = (myAppsState as? Resource.Success)?.data?.any { app ->
                (app.gig?.id == gig?.id) && (
                    app.status.equals("accepted", ignoreCase = true) ||
                    app.status.equals("in_progress", ignoreCase = true) ||
                    app.status.equals("work_submitted", ignoreCase = true)
                )
            } == true
            acceptedByGig || acceptedByApp
        }
    }

    LaunchedEffect(gigId) {
        viewModel.loadGigById(gigId)
        appViewModel.loadMyApplications()
    }

    LaunchedEffect(gig, isOwner) {
        if (gig != null && isOwner) {
            appViewModel.loadApplicationsForGig(gig!!.id)
        }
    }

    var ui by remember { mutableStateOf(GigDetailUiState()) }
    val snackbar = remember { SnackbarHostState() }
    val scope    = rememberCoroutineScope()
    val scroll   = rememberScrollState()

    // Collapsing hero — derivedStateOf so only scroll triggers recompute
    val heroHeightDp     = 220f
    val minHeroHeightDp  = 56f
    val collapseProgress by remember {
        derivedStateOf {
            val maxScroll = (heroHeightDp - minHeroHeightDp) * 3.5f
            (scroll.value / maxScroll).coerceIn(0f, 1f)
        }
    }

    // Reset budget default once gig loads
    LaunchedEffect(gig) {
        if (gig != null && ui.expectedBudget.isEmpty()) {
            ui = ui.copy(expectedBudget = gig!!.budget.toInt().toString())
        }
    }

    // React to apply state
    LaunchedEffect(applyState) {
        when (val s = applyState) {
            is Resource.Success -> {
                ui = ui.copy(showApplySheet = false)
                scope.launch { snackbar.showSnackbar("Application submitted! 🎉") }
                appViewModel.resetApplyState()
            }
            is Resource.Error -> {
                scope.launch { snackbar.showSnackbar("Failed: ${s.message}") }
                appViewModel.resetApplyState()
            }
            else -> {}
        }
    }

    // React to submit work state
    LaunchedEffect(submitWorkState) {
        when (val s = submitWorkState) {
            is Resource.Success -> {
                ui = ui.copy(showSubmitWorkSheet = false, submittedUrl = "", submittedNote = "")
                scope.launch { snackbar.showSnackbar("Work submitted successfully! 🎉 Client has been notified.") }
                viewModel.resetSubmitWorkState()
            }
            is Resource.Error -> {
                scope.launch { snackbar.showSnackbar("Work submission failed: ${s.message}") }
                viewModel.resetSubmitWorkState()
            }
            else -> {}
        }
    }

    // React to OTP state (employer requesting code)
    LaunchedEffect(otpState) {
        when (val s = otpState) {
            is Resource.Success -> {
                ui = ui.copy(displayedOtp = s.data.otp)
                scope.launch { snackbar.showSnackbar("Completion OTP: ${s.data.otp}") }
                viewModel.resetOtpState()
            }
            is Resource.Error -> {
                scope.launch { snackbar.showSnackbar("OTP fetch failed: ${s.message}") }
                viewModel.resetOtpState()
            }
            else -> {}
        }
    }

    // React to complete gig state
    LaunchedEffect(completeGigState) {
        when (val s = completeGigState) {
            is Resource.Success -> {
                ui = ui.copy(showOtpSheet = false, otpInput = "")
                scope.launch { snackbar.showSnackbar("Gig Completed Successfully! 🎉 Time to leave a review!") }
                viewModel.resetCompleteGigState()
            }
            is Resource.Error -> {
                scope.launch { snackbar.showSnackbar("Completion failed: ${s.message}") }
                viewModel.resetCompleteGigState()
            }
            else -> {}
        }
    }

    // ── Action Handler ──────────────────────────────────────────────────────
    val onAction: (GigDetailAction) -> Unit = { action ->
        when (action) {
            GigDetailAction.ToggleSave         -> ui = ui.copy(isSaved = !ui.isSaved)
            GigDetailAction.OpenApplySheet     -> ui = ui.copy(showApplySheet = true)
            GigDetailAction.DismissApplySheet  -> ui = ui.copy(showApplySheet = false, proposalError = "")
            GigDetailAction.ToggleDescription  -> ui = ui.copy(descExpanded = !ui.descExpanded)
            is GigDetailAction.ProposalChanged -> ui = ui.copy(proposal = action.v, proposalError = "")
            is GigDetailAction.BudgetChanged   -> ui = ui.copy(expectedBudget = action.v)
            is GigDetailAction.Submit -> {
                if (ui.proposal.trim().length < 30) {
                    ui = ui.copy(proposalError = "Please write at least 30 characters in your proposal")
                } else {
                    val budget = ui.expectedBudget.toDoubleOrNull() ?: action.defaultBudget
                    action.onSubmit(ui.proposal.trim(), budget)
                }
            }

            GigDetailAction.OpenSubmitWorkSheet    -> ui = ui.copy(showSubmitWorkSheet = true)
            GigDetailAction.DismissSubmitWorkSheet -> ui = ui.copy(showSubmitWorkSheet = false, submittedUrlError = "")
            is GigDetailAction.SubmittedUrlChanged -> ui = ui.copy(submittedUrl = action.v, submittedUrlError = "")
            is GigDetailAction.SubmittedNoteChanged -> ui = ui.copy(submittedNote = action.v)
            is GigDetailAction.PerformSubmitWork -> {
                if (ui.submittedUrl.trim().isBlank()) {
                    ui = ui.copy(submittedUrlError = "Please enter a deliverable URL (e.g. GitHub, Drive, Figma)")
                } else {
                    action.onSubmit(ui.submittedUrl.trim(), ui.submittedNote.trim())
                }
            }

            GigDetailAction.OpenOtpSheet    -> ui = ui.copy(showOtpSheet = true)
            GigDetailAction.DismissOtpSheet -> ui = ui.copy(showOtpSheet = false, otpError = "")
            is GigDetailAction.OtpInputChanged -> ui = ui.copy(otpInput = action.v, otpError = "")
            is GigDetailAction.PerformCompleteGig -> {
                if (ui.otpInput.trim().length < 4) {
                    ui = ui.copy(otpError = "Please enter the complete 4-digit OTP code")
                } else {
                    action.onSubmit(ui.otpInput.trim())
                }
            }
            GigDetailAction.RequestOtp -> {
                gig?.let { viewModel.requestCompletionOtp(it.id) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Scrollable content ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            when (val state = gigState) {
                null, is Resource.Loading -> {
                    Spacer(Modifier.height(heroHeightDp.dp))
                    DetailLoadingState()
                }
                is Resource.Error -> {
                    Spacer(Modifier.height(heroHeightDp.dp))
                    DetailErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadGigById(gigId) }
                    )
                }
                is Resource.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val gigData = (state as Resource.Success<Gig>).data

                    // Hero spacer — hero is overlaid as a fixed element
                    Spacer(Modifier.height(heroHeightDp.dp))

                    // Content card lifts 20dp over the hero bottom edge
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-20).dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp)
                    ) {
                        // ── Status + Deadline row ─────────────────────────
                        StatusDeadlineRow(gig = gigData)

                        // ── Gig Title ─────────────────────────────────────
                        Spacer(Modifier.height(14.dp))
                        Text(
                            gigData.title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight  = 32.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // ── Poster Card ───────────────────────────────────
                        gigData.employer?.let { employer ->
                            Spacer(Modifier.height(16.dp))
                            PosterCard(
                                employer = employer,
                                onViewProfile = {
                                    navController.navigate(Routes.publicProfile(employer.id))
                                }
                            )
                        }

                        // ── Budget & Deadline Card (gradient border) ───────
                        Spacer(Modifier.height(16.dp))
                        BudgetDeadlineCard(gig = gigData)

                        // ── About This Gig ────────────────────────────────
                        Spacer(Modifier.height(24.dp))
                        SectionLabel("About This Gig")
                        Spacer(Modifier.height(8.dp))
                        ExpandableDescription(
                            text      = gigData.description,
                            expanded  = ui.descExpanded,
                            onToggle  = { onAction(GigDetailAction.ToggleDescription) }
                        )

                        // ── Skills Required ───────────────────────────────
                        if (gigData.skills.isNotEmpty()) {
                            Spacer(Modifier.height(22.dp))
                            SectionLabel("Skills Required")
                            Spacer(Modifier.height(10.dp))
                            SkillsFlowRow(skills = gigData.skills)
                        }

                        // ── Location ──────────────────────────────────────
                        Spacer(Modifier.height(20.dp))
                        LocationRow(location = gigData.location)

                        // ── Application Stats ─────────────────────────────
                        Spacer(Modifier.height(20.dp))
                        ApplicationStatRow(count = gigData.applicationsCount)

                        // ── Owner: Work Submission Review Panel (when status = work_submitted) ─
                        if (isOwner && gigData.isWorkSubmitted()) {
                            Spacer(Modifier.height(28.dp))
                            SectionLabel("Deliverable Review 📦")
                            Spacer(Modifier.height(12.dp))
                            WorkSubmissionPanel(
                                gig = gigData,
                                displayedOtp = ui.displayedOtp,
                                onOpenOtpSheet = { onAction(GigDetailAction.OpenOtpSheet) },
                                onRequestOtp = { onAction(GigDetailAction.RequestOtp) }
                            )
                        }

                        // ── Owner: Applicant list ─────────────────────────
                        if (isOwner && !gigData.isWorkSubmitted() && !gigData.isCompleted()) {
                            Spacer(Modifier.height(28.dp))
                            SectionLabel("Applicants")
                            Spacer(Modifier.height(12.dp))

                            val appsState by appViewModel.gigApplications.collectAsState()
                            ApplicantSection(
                                appsState       = appsState,
                                onStatusUpdate  = { appId, newStatus ->
                                    appViewModel.updateApplicationStatus(appId, newStatus) { ok ->
                                        if (ok) {
                                            appViewModel.loadApplicationsForGig(gigData.id)
                                            viewModel.loadGigById(gigData.id)
                                        }
                                    }
                                },
                                onChatClick = { applicantId, applicantName ->
                                    navController.navigate(
                                        Routes.chat(
                                            receiverId   = applicantId,
                                            receiverName = applicantName,
                                                                                        gigTitle     = gigData.title,
                                            gigBudget    = gigData.formattedBudget(),
                                            // CRITICAL: scope this chat to the gig so G1 and G2
                                            // conversations between the same two users stay isolated.
                                            gigId        = gigData.id
                                        )
                                    )
                                }
                            )
                        }

                        // Bottom padding for action bar clearance
                        Spacer(Modifier.height(112.dp))
                    }
                }
            }
        }

        // ── Fixed collapsing hero ───────────────────────────────────────────
        gig?.let { gigData ->
            CollapsingHero(
                gig             = gigData,
                collapseProgress = collapseProgress,
                isSaved         = ui.isSaved,
                onBack          = { navController.popBackStack() },
                onToggleSave    = { onAction(GigDetailAction.ToggleSave) },
                onShare         = { /* TODO: implement share intent */ }
            )
        } ?: run {
            // Minimal back button while loading
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .offset(x = 12.dp, y = 12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
            }
        }

        // ── Sticky Bottom Action Bar ────────────────────────────────────────
        gig?.let { gigData ->
            if (!isOwner) {
                StickyActionBar(
                    gig              = gigData,
                    isAcceptedWorker = isAcceptedWorker,
                    onApply          = { onAction(GigDetailAction.OpenApplySheet) },
                    onSubmitWork     = { onAction(GigDetailAction.OpenSubmitWorkSheet) },
                    modifier         = Modifier.align(Alignment.BottomCenter)
                )
            } else if (gigData.isInProgress()) {
                OwnerInProgressActionBar(
                    gig = gigData,
                    onChatClick = {
                        val workerId = gigData.acceptedApplicant ?: ""
                        navController.navigate(
                            Routes.chat(
                                receiverId   = workerId,
                                receiverName = "Hired Student",
                                gigTitle     = gigData.title,
                                gigBudget    = gigData.formattedBudget(),
                                gigId        = gigData.id
                            )
                        )
                    },
                    onOpenOtpSheet = {
                        onAction(GigDetailAction.RequestOtp)
                        onAction(GigDetailAction.OpenOtpSheet)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbar,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }

    // ── Apply Sheet ─────────────────────────────────────────────────────────
    if (ui.showApplySheet) {
        gig?.let { gigData ->
            ApplyBottomSheet(
                gig           = gigData,
                proposal      = ui.proposal,
                expectedBudget = ui.expectedBudget,
                proposalError = ui.proposalError,
                isSubmitting  = applyState is Resource.Loading,
                onDismiss     = { onAction(GigDetailAction.DismissApplySheet) },
                onProposalChanged = { onAction(GigDetailAction.ProposalChanged(it)) },
                onBudgetChanged   = { onAction(GigDetailAction.BudgetChanged(it)) },
                onSubmit = {
                    onAction(
                        GigDetailAction.Submit(gigData.budget) { proposal, budget ->
                            appViewModel.applyForGig(gigData.id, proposal, budget)
                        }
                    )
                }
            )
        }
    }

    // ── Submit Work Sheet (Worker) ──────────────────────────────────────────
    if (ui.showSubmitWorkSheet) {
        gig?.let { gigData ->
            SubmitWorkBottomSheet(
                gig               = gigData,
                submittedUrl      = ui.submittedUrl,
                submittedNote     = ui.submittedNote,
                urlError          = ui.submittedUrlError,
                isSubmitting      = submitWorkState is Resource.Loading,
                onDismiss         = { onAction(GigDetailAction.DismissSubmitWorkSheet) },
                onUrlChanged      = { onAction(GigDetailAction.SubmittedUrlChanged(it)) },
                onNoteChanged     = { onAction(GigDetailAction.SubmittedNoteChanged(it)) },
                onSubmit          = {
                    onAction(
                        GigDetailAction.PerformSubmitWork { url, note ->
                            viewModel.submitWork(gigData.id, url, note)
                        }
                    )
                }
            )
        }
    }

    // ── Complete OTP Sheet (Employer) ───────────────────────────────────────
    if (ui.showOtpSheet) {
        gig?.let { gigData ->
            EnterOtpBottomSheet(
                gig          = gigData,
                otpInput     = if (ui.otpInput.isNotBlank()) ui.otpInput else ui.displayedOtp,
                otpError     = ui.otpError,
                isSubmitting = completeGigState is Resource.Loading,
                onDismiss    = { onAction(GigDetailAction.DismissOtpSheet) },
                onOtpChanged = { onAction(GigDetailAction.OtpInputChanged(it)) },
                onSubmit     = {
                    val codeToUse = if (ui.otpInput.isNotBlank()) ui.otpInput else ui.displayedOtp
                    onAction(
                        GigDetailAction.PerformCompleteGig { _ ->
                            viewModel.completeGig(gigData.id, codeToUse)
                        }
                    )
                }
            )
        }
    }
}

// ─── Collapsing Hero ──────────────────────────────────────────────────────────

@Composable
private fun CollapsingHero(
    gig:              Gig,
    collapseProgress: Float,
    isSaved:          Boolean,
    onBack:           () -> Unit,
    onToggleSave:     () -> Unit,
    onShare:          () -> Unit
) {
    val heroHeightDp = 220f
    val minHeightDp  = 56f
    val currentHeight = (heroHeightDp - ((heroHeightDp - minHeightDp) * collapseProgress)).dp
    val gradientColors = categoryGradient(gig.category)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(currentHeight)
            .background(
                Brush.linearGradient(
                    colors = gradientColors,
                    start  = Offset(0f, 0f),
                    end    = Offset(1400f, 700f)
                )
            )
    ) {
        // ── Expanded state content ────────────────────────────────────────
        if (collapseProgress < 0.7f) {
            val alpha = (1f - collapseProgress / 0.7f).coerceIn(0f, 1f)

            // Decorative grid pattern overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha * 0.06f }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White, Color.Transparent),
                            center = Offset(300f, 80f),
                            radius = 600f
                        )
                    )
            )

            // Category emoji icon centered
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(categoryIcon(gig.category), fontSize = 30.sp)
                    }
                    Text(
                        gig.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight    = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            // Bottom scrim fade into MaterialTheme.colorScheme.background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomStart)
                    .graphicsLayer { this.alpha = alpha }
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))
                    )
            )
        }

        // ── Top action row (always visible) ─────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(22.dp))
            }

            // Collapsed title slides in
            if (collapseProgress > 0.45f) {
                val titleAlpha = ((collapseProgress - 0.45f) / 0.35f).coerceIn(0f, 1f)
                Text(
                    gig.title,
                    style    = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp
                    ),
                    color    = Color.White.copy(alpha = titleAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(Modifier.weight(1f))
            }

            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, "Share", tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onToggleSave) {
                Icon(
                    if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    "Bookmark",
                    tint     = if (isSaved) GradientIndigoEnd else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Status + Deadline Row ────────────────────────────────────────────────────

@Composable
private fun StatusDeadlineRow(gig: Gig) {
    val (statusColor, statusLabel) = when (gig.status.lowercase()) {
        "open"        -> SemanticSuccess to "Open for Applications"
        "in_progress" -> SemanticWarning to "Work in Progress"
        "completed"   -> Color(0xFF14B8A6) to "Completed"
        "closed"      -> SemanticError to "Closed"
        else          -> SemanticSuccess to "Open"
    }

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Status pill with live dot
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(statusColor.copy(alpha = 0.12f))
                .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                statusLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 11.sp
                ),
                color = statusColor
            )
        }

        // Deadline display
        if (gig.deadline.isNotBlank()) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                Text(
                    "Due ${gig.deadline.take(10)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ─── Poster Card ──────────────────────────────────────────────────────────────

@Composable
private fun PosterCard(
    employer:     com.abpvt.campusgig_frontend.data.model.User,
    onViewProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .detailCard()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarInitials(name = employer.name, size = 42)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    employer.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                // Verified badge
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D9488).copy(alpha = 0.2f))
                        .border(BorderStroke(1.dp, Color(0xFF0D9488)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 8.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.ExtraBold)
                }
            }

            Text(
                employer.college,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            if (employer.reviewCount > 0) {
                Spacer(Modifier.height(3.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(5) { i ->
                        Icon(
                            Icons.Default.Star, null,
                            tint     = if (i < employer.rating.toInt()) SemanticWarning else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "%.1f (${employer.reviewCount})".format(employer.rating),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            "View →",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp
            ),
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onViewProfile
            )
        )
    }
}

// ─── Budget & Deadline Card (gradient border) ─────────────────────────────────

@Composable
private fun BudgetDeadlineCard(gig: Gig) {
    // Outer box provides the gradient "border" via padding trick
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, Violet500))
            )
            .padding(1.dp)  // border thickness
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Budget column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "BUDGET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    gig.formattedBudget(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 26.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    if (gig.duration.isNotBlank()) gig.duration else "Fixed price",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )

            // Deadline column
            Column(
                modifier  = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    "DEADLINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (gig.deadline.isNotBlank()) gig.deadline.take(10) else "Flexible",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Application deadline",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ─── Expandable Description ───────────────────────────────────────────────────

@Composable
private fun ExpandableDescription(
    text:     String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val needsToggle = text.length > 280 || text.lines().size > 5

    Text(
        text      = text,
        style     = MaterialTheme.typography.bodyMedium.copy(
            fontSize   = 14.sp,
            lineHeight  = 22.sp
        ),
        color     = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines  = if (expanded) Int.MAX_VALUE else 5,
        overflow  = TextOverflow.Ellipsis,
        modifier  = Modifier.animateContentSize(tween(260))
    )

    if (needsToggle) {
        Spacer(Modifier.height(6.dp))
        Text(
            if (expanded) "Show less ↑" else "Read more →",
            style    = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp
            ),
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onToggle
            )
        )
    }
}

// ─── Skills FlowRow ───────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillsFlowRow(skills: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        skills.forEach { skill ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GlowIndigo)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Default.Code, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                Text(
                    skill,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ─── Location Row ─────────────────────────────────────────────────────────────

@Composable
private fun LocationRow(location: String) {
    val displayText = when {
        location.isBlank() || location.lowercase() == "remote" ->
            "Remote — work from anywhere"
        location.lowercase().contains("campus") ->
            "On Campus — $location"
        else -> location
    }

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier              = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Text(
            displayText,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Application Stat Row ─────────────────────────────────────────────────────

@Composable
private fun ApplicationStatRow(count: Int) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Overlapping mock avatar stack
        val dotColors = listOf(
            Color(0xFF6366F1), Color(0xFF14B8A6), Color(0xFFF59E0B), Color(0xFFEC4899)
        )
        Box(modifier = Modifier.width(((dotColors.size - 1) * 16 + 24).dp)) {
            dotColors.forEachIndexed { i, color ->
                Box(
                    modifier = Modifier
                        .offset(x = (i * 16).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.background), CircleShape)
                )
            }
        }

        Icon(Icons.Default.People, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
        val safeCount = kotlin.math.max(0, count)
        Text(
            buildString {
                append(safeCount)
                append(if (safeCount == 1) " student has applied" else " students have applied")
            },
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Section Label ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 15.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
}

// ─── Applicant Section ────────────────────────────────────────────────────────

@Composable
private fun ApplicantSection(
    appsState:      Resource<List<Application>>?,
    onStatusUpdate: (String, String) -> Unit,
    onChatClick:    (String, String) -> Unit
) {
    when (val state = appsState) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }

        is Resource.Error -> {
            Text(
                "Failed to load applicants: ${state.message}",
                style = MaterialTheme.typography.bodySmall,
                color = SemanticError
            )
        }

        is Resource.Success -> {
            val applications = state.data
            if (applications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .detailCard()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📭", fontSize = 28.sp)
                        Text(
                            "No applications received yet",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    applications.forEach { application ->
                        ApplicantCard(
                            application    = application,
                            onStatusUpdate = { newStatus -> onStatusUpdate(application.id, newStatus) },
                            onChatClick    = {
                                val id   = application.applicant?.id ?: return@ApplicantCard
                                val name = application.applicant.name
                                onChatClick(id, name)
                            }
                        )
                    }
                }
            }
        }

        null -> {}
    }
}

// ─── Applicant Card ───────────────────────────────────────────────────────────

@Composable
private fun ApplicantCard(
    application:    Application,
    onStatusUpdate: (String) -> Unit,
    onChatClick:    () -> Unit
) {
    val applicant = application.applicant
    val status    = application.status.lowercase()

    val (statusColor, statusBg) = when (status) {
        "accepted"   -> SemanticSuccess to SemanticSuccessBg
        "rejected"   -> SemanticError   to SemanticErrorBg
        "withdrawn"  -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)    to MaterialTheme.colorScheme.surfaceVariant
        else         -> SemanticWarning to MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .detailCard()
            .padding(14.dp)
    ) {
        // Applicant identity row
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarInitials(name = applicant?.name ?: "C", size = 38)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    applicant?.name ?: "Candidate",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    applicant?.college ?: "College",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(statusBg)
                    .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)), RoundedCornerShape(999.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    status.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 10.sp
                    ),
                    color = statusColor
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Proposal box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(10.dp)
        ) {
            Text(
                "PROPOSAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                application.proposal,
                style   = MaterialTheme.typography.bodySmall.copy(
                    fontSize   = 13.sp,
                    lineHeight  = 19.sp
                ),
                color   = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(10.dp))

        // Bid + action buttons
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Bid amount
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "₹${application.expectedBudget.toInt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 16.sp
                    ),
                    color = SemanticSuccess
                )
                Text(
                    "bid",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (status == "pending") {
                    // Reject button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SemanticErrorBg)
                            .border(BorderStroke(1.dp, SemanticError.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null
                            ) { onStatusUpdate("rejected") }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            "Reject",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SemanticError
                        )
                    }
                    // Accept button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SemanticSuccessBg)
                            .border(BorderStroke(1.dp, SemanticSuccess.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null
                            ) { onStatusUpdate("accepted") }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle, null,
                                tint     = SemanticSuccess,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                "Accept",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SemanticSuccess
                            )
                        }
                    }
                } else if (status == "accepted" || status == "in_progress") {
                    // Chat button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GlowIndigo)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)), RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null,
                                onClick           = onChatClick
                            )
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            "Chat →",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// ─── Sticky Bottom Action Bar ─────────────────────────────────────────────────

@Composable
private fun StickyActionBar(
    gig:              Gig,
    isAcceptedWorker: Boolean = false,
    onApply:          () -> Unit,
    onSubmitWork:     () -> Unit = {},
    modifier:         Modifier = Modifier
) {
    val (ctaLabel, ctaEnabled, ctaColors, onClickAction) = when {
        gig.isCompleted() ->
            Tuple4("Gig Completed ✓", false, listOf(Color(0xFF14B8A6), Color(0xFF14B8A6)), {})

        isAcceptedWorker && gig.isInProgress() ->
            Tuple4("Submit Work 🚀", true, listOf(GradientIndigoStart, GradientIndigoEnd), onSubmitWork)

        isAcceptedWorker && gig.isWorkSubmitted() ->
            Tuple4("Work Submitted ⏳", false, listOf(Color(0xFFF59E0B), Color(0xFFF59E0B)), {})

        gig.isWorkSubmitted() ->
            Tuple4("Work Submitted", false, listOf(Color(0xFFF59E0B), Color(0xFFF59E0B)), {})

        gig.isInProgress() ->
            Tuple4("Work in Progress", false, listOf(SemanticWarning, SemanticWarning), {})

        !gig.isOpen() ->
            Tuple4("Applications Closed", false, listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.surfaceContainerHigh), {})

        else ->
            Tuple4("Apply Now →", true, listOf(GradientIndigoStart, GradientIndigoEnd), onApply)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xF50F1117))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(0.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .height(50.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Budget display
            Column {
                Text(
                    gig.formattedBudget(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    if (gig.duration.isNotBlank()) gig.duration else "Fixed price",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // CTA button
            Box(
                modifier = Modifier
                    .width(158.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Brush.linearGradient(ctaColors))
                    .then(
                        if (ctaEnabled) Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                            onClick           = onClickAction
                        ) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    ctaLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    ),
                    color = if (ctaEnabled) Color.White else Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun OwnerInProgressActionBar(
    gig: Gig,
    onChatClick: () -> Unit,
    onOpenOtpSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xF50F1117))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(0.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Work in Progress ⏳",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    color = Color(0xFFF59E0B)
                )
                Text(
                    "Awaiting completion confirmation",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(25.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(25.dp))
                        .clickable { onChatClick() }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Chat 💬",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(25.dp))
                        .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                        .clickable { onOpenOtpSheet() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Approve & Complete 🔑",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ─── Apply Bottom Sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplyBottomSheet(
    gig:              Gig,
    proposal:         String,
    expectedBudget:   String,
    proposalError:    String,
    isSubmitting:     Boolean,
    onDismiss:        () -> Unit,
    onProposalChanged: (String) -> Unit,
    onBudgetChanged:   (String) -> Unit,
    onSubmit:          () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "Submit Your Proposal",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 18.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                gig.title,
                style    = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(20.dp))

            // Proposal field
            OutlinedTextField(
                value         = proposal,
                onValueChange = onProposalChanged,
                textStyle     = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                label         = {
                    Text("Your Proposal *", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)
                },
                placeholder   = {
                    Text(
                        "Explain why you're the perfect fit, your approach, and relevant experience…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f).copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                maxLines      = 8,
                isError       = proposalError.isNotBlank(),
                shape         = RoundedCornerShape(14.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedTextColor    = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor  = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor      = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                    errorBorderColor        = SemanticError,
                    cursorColor             = MaterialTheme.colorScheme.primary
                )
            )

            if (proposalError.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    proposalError,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = SemanticError
                )
            }

            Spacer(Modifier.height(12.dp))

            // Budget field
            OutlinedTextField(
                value         = expectedBudget,
                onValueChange = onBudgetChanged,
                textStyle     = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                label         = {
                    Text("Your Expected Budget (₹)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)
                },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedTextColor    = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor  = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor      = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                    cursorColor             = MaterialTheme.colorScheme.primary
                )
            )

            // Budget hint
            Spacer(Modifier.height(4.dp))
            Text(
                "Poster's budget: ${gig.formattedBudget()}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(22.dp))

            // Submit button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .indigoGradientBg(radius = 14)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = { if (!isSubmitting) onSubmit() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(
                            "Submit Application",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ─── Detail Loading State ─────────────────────────────────────────────────────

@Composable
private fun DetailLoadingState() {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .height(500.dp),
        contentAlignment = Alignment.Center
    ) {
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
                "Loading gig details…",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// ─── Detail Error State ───────────────────────────────────────────────────────

@Composable
private fun DetailErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .height(500.dp),
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
                color     = SemanticError
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

// ─── Work Submission Panel (Employer View) ──────────────────────────────────

@Composable
private fun WorkSubmissionPanel(
    gig: Gig,
    displayedOtp: String,
    onOpenOtpSheet: () -> Unit,
    onRequestOtp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1AF59E0B))
            .border(BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
                Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("📦", fontSize = 20.sp)
            Text(
                "Student Submitted Work",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = Color(0xFFF59E0B)
            )
        }

        // ── Deliverable link ───────────────────────────────────────────────
        if (gig.submittedWorkUrl.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "DELIVERABLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    gig.submittedWorkUrl,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (gig.submittedWorkNote.isNotBlank()) {
                    Text(
                        gig.submittedWorkNote,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                "The worker has submitted their deliverable. Review it, then enter the 4-digit completion code to complete the gig.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Action buttons: Enter OTP and Refresh/View OTP Code
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF59E0B))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenOtpSheet
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Approve & Enter OTP 🔑",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRequestOtp
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (displayedOtp.isNotBlank()) "Code: $displayedOtp" else "Get Code",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

// ─── Submit Work Bottom Sheet (Worker) ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitWorkBottomSheet(
    gig: Gig,
    submittedUrl: String,
    submittedNote: String,
    urlError: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onUrlChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "Submit Completed Work 🚀",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                gig.title,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = submittedUrl,
                onValueChange = onUrlChanged,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                label = { Text("Deliverable Link / URL *", style = MaterialTheme.typography.labelMedium) },
                placeholder = { Text("https://github.com/... or Google Drive link", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = urlError.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = SemanticError,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            if (urlError.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(urlError, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SemanticError)
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = submittedNote,
                onValueChange = onNoteChanged,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                label = { Text("Handover Note (Optional)", style = MaterialTheme.typography.labelMedium) },
                placeholder = { Text("Explain setup steps or key highlights for the employer…", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(24.dp))

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                    )
                    .clickable(enabled = !isSubmitting) { if (!isSubmitting) onSubmit() },
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                } else {
                    Text(
                        "Submit Deliverable Now 🚀",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ─── Enter OTP Bottom Sheet (Employer) ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterOtpBottomSheet(
    gig: Gig,
    otpInput: String,
    otpError: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onOtpChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Spacer(Modifier.height(24.dp))

            // Key icon in gradient circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🔑", fontSize = 28.sp)
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Approve & Complete Gig",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "\"${gig.title}\"",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(24.dp))

            // ── OTP Display Box ───────────────────────────────────────────────
            val displayCode = otpInput.trim()

            if (displayCode.length == 4) {
                // Show the 4 digits prominently
                Text(
                    "Your Completion Code",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayCode.forEach { digit ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFFF59E0B).copy(alpha = 0.15f),
                                            Color(0xFFD97706).copy(alpha = 0.08f)
                                        )
                                    )
                                )
                                .border(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                digit.toString(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp
                                ),
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "This code was sent to your Notifications",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                )
            } else {
                // Code not yet loaded — show loading or prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Generating your approval code…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (otpError.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    otpError,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = SemanticError,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Approve button ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (!isSubmitting && displayCode.length == 4)
                            Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                        else
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )
                    )
                    .clickable(enabled = !isSubmitting && displayCode.length == 4) {
                        if (!isSubmitting) onSubmit()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "✅  Approve & Complete Gig",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Tapping this will mark the gig as Completed and notify the worker.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
