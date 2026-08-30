/**
 * CreateGigScreen.kt — v3.0 — 4-Step Gig Creation Wizard
 *
 * Architecture:
 *  - Single immutable GigFormState data class owns ALL form fields + step state.
 *  - Sealed GigFormAction drives every mutation through one onAction() lambda.
 *  - Validation is pure logic: validate(step, state): String — testable, no side effects.
 *  - No form state escapes into sub-composable bodies.
 *
 * Visual Spec (Linear / Raycast / Stripe inspired):
 *  - Segmented progress bar with filled / gradient-active / unfilled states.
 *  - Category grid: 2-col, per-category color, top-right checkmark on selection.
 *  - Skill tag input with keyboard "Add" action and dismissible pills.
 *  - Location type: 3-card horizontal picker with icon + label + sub-label.
 *  - Budget: pill segmented toggle, underline-only large amount input, range chips, market rate card.
 *  - Review: PREVIEW label + checklist rows, terms checkbox, "Post Gig 🚀" CTA.
 *  - Sticky bottom nav bar: Back (ghost) + Continue (gradient).
 *  - Step-scoped inline error banner above the nav bar.
 */
package com.abpvt.campusgig_frontend.features.gigs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Work
import java.util.Calendar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.GigViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Gig
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

// ─── Domain Data ──────────────────────────────────────────────────────────────

private data class CategoryItem(
    val name:  String,
    val icon:  ImageVector,
    val color: Color,
    val emoji: String
)

private val GIG_CATEGORY_ITEMS = listOf(
    CategoryItem("Coding",      Icons.Default.Code,     Color(0xFF6366F1), "⌨️"),
    CategoryItem("Design",      Icons.Default.Edit,     Color(0xFFEC4899), "🎨"),
    CategoryItem("Writing",     Icons.Default.Edit,     Color(0xFF14B8A6), "✍️"),
    CategoryItem("Tutoring",    Icons.Default.Star,     Color(0xFFF59E0B), "📚"),
    CategoryItem("Photography", Icons.Default.Image,    Color(0xFFEF4444), "📷"),
    CategoryItem("Video",       Icons.Default.Videocam, Color(0xFF3B82F6), "🎬"),
    CategoryItem("Marketing",   Icons.Default.Mic,      Color(0xFFA855F7), "📣"),
    CategoryItem("Other",       Icons.Default.Work,     Color(0xFF64748B), "💼"),
)

private val MARKET_RATES = mapOf(
    "Coding"      to "₹800–₹3,000",
    "Design"      to "₹500–₹2,500",
    "Writing"     to "₹300–₹1,500",
    "Tutoring"    to "₹200–₹800",
    "Photography" to "₹1,000–₹4,000",
    "Video"       to "₹1,500–₹5,000",
    "Marketing"   to "₹500–₹2,000",
    "Other"       to "₹300–₹1,500"
)

private val BUDGET_RANGE_CHIPS = listOf(
    "< ₹500"  to "400",
    "₹500–1K" to "750",
    "₹1K–3K"  to "2000",
    "₹3K–5K"  to "4000",
    "₹5K+"    to "6000"
)

private val LOCATION_OPTIONS = listOf(
    Triple("Remote",    "🌐", "Work from anywhere"),
    Triple("On Campus", "🏫", "In-person at college"),
    Triple("Hybrid",    "🔄",  "Mix of both")
)

private val STEP_LABELS = listOf("Basics", "Details", "Budget", "Review")

// ─── UI State ─────────────────────────────────────────────────────────────────

private data class GigFormState(
    // Step navigation
    val step:             Int     = 0,

    // Step 1 — Basics
    val category:         String  = "",
    val title:            String  = "",
    val description:      String  = "",

    // Step 2 — Details
    val skills:           List<String> = emptyList(),
    val skillInput:       String  = "",
    val deadline:         String  = "",
    val location:         String  = "Remote",

    // Step 3 — Budget
    val budgetType:       String  = "Fixed Price",   // "Fixed Price" | "Hourly Rate"
    val budgetAmount:     String  = "",

    // Step 4 — Review
    val termsAccepted:    Boolean = false,

    // Validation
    val stepError:        String  = ""
)

// ─── UI Actions ───────────────────────────────────────────────────────────────

private sealed interface GigFormAction {
    object NextStep                                    : GigFormAction
    object PrevStep                                    : GigFormAction
    data class CategorySelected(val v: String)         : GigFormAction
    data class TitleChanged(val v: String)             : GigFormAction
    data class DescriptionChanged(val v: String)       : GigFormAction
    data class SkillInputChanged(val v: String)        : GigFormAction
    object AddSkill                                    : GigFormAction
    data class RemoveSkill(val skill: String)          : GigFormAction
    data class DeadlineChanged(val v: String)          : GigFormAction
    data class LocationSelected(val v: String)         : GigFormAction
    data class BudgetTypeSelected(val v: String)       : GigFormAction
    data class BudgetAmountChanged(val v: String)      : GigFormAction
    data class BudgetRangeSelected(val midpoint: String): GigFormAction
    object ToggleTerms                                 : GigFormAction
    data class PostGig(val onPost: (Gig) -> Unit)      : GigFormAction
}

// ─── Step Validation ─────────────────────────────────────────────────────────

private fun validateStep(step: Int, s: GigFormState): String = when (step) {
    0 -> when {
        s.category.isBlank()   -> "Please select a category"
        s.title.length < 10    -> "Title must be at least 10 characters"
        s.description.isBlank() -> "Please write a description"
        else                   -> ""
    }
    2 -> when {
        s.budgetAmount.isBlank() -> "Please enter a budget amount"
        else                    -> ""
    }
    3 -> when {
        !s.termsAccepted         -> "Please accept the terms to post your gig"
        s.category.isBlank()     -> "Category is missing — go back to Step 1"
        s.budgetAmount.isBlank() -> "Budget is missing — go back to Step 3"
        else                     -> ""
    }
    else -> ""
}

// ─── Modifier Extensions ──────────────────────────────────────────────────────

@Composable
private fun Modifier.formCard(radius: Int = 12): Modifier = this
    .clip(RoundedCornerShape(radius.dp))
    .background(MaterialTheme.colorScheme.surface)
    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(radius.dp))

private fun Modifier.indigoGradient(radius: Int = 14): Modifier = this
    .clip(RoundedCornerShape(radius.dp))
    .background(
        Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoMid, GradientIndigoEnd))
    )

// ─── Field Colors Helper ──────────────────────────────────────────────────────

@Composable
private fun gigFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor       = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor     = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor  = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor     = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor   = MaterialTheme.colorScheme.outline,
    errorBorderColor       = SemanticError,
    cursorColor            = MaterialTheme.colorScheme.primary,
    focusedLabelColor      = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateGigScreen(
    navController: NavController,
    viewModel: GigViewModel = viewModel(
        factory = GigViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).gigRepository
        )
    )
) {
    var form by remember { mutableStateOf(GigFormState()) }
    val createState by viewModel.createState.collectAsState()

    LaunchedEffect(createState) {
        if (createState is Resource.Success) {
            viewModel.resetCreateState()
            navController.popBackStack()
        }
    }

    // ── Action handler ──────────────────────────────────────────────────────
    val onAction: (GigFormAction) -> Unit = { action ->
        when (action) {
            GigFormAction.NextStep -> {
                val err = validateStep(form.step, form)
                if (err.isEmpty()) form = form.copy(step = form.step + 1, stepError = "")
                else form = form.copy(stepError = err)
            }
            GigFormAction.PrevStep -> {
                if (form.step > 0) form = form.copy(step = form.step - 1, stepError = "")
                else navController.popBackStack()
            }
            is GigFormAction.CategorySelected    -> form = form.copy(category = action.v, stepError = "")
            is GigFormAction.TitleChanged        -> form = form.copy(title = action.v.take(100), stepError = "")
            is GigFormAction.DescriptionChanged  -> form = form.copy(description = action.v.take(2000))
            is GigFormAction.SkillInputChanged   -> form = form.copy(skillInput = action.v)
            GigFormAction.AddSkill -> {
                val trimmed = form.skillInput.trim()
                if (trimmed.isNotBlank() && trimmed !in form.skills) {
                    form = form.copy(skills = form.skills + trimmed, skillInput = "")
                }
            }
            is GigFormAction.RemoveSkill         -> form = form.copy(skills = form.skills - action.skill)
            is GigFormAction.DeadlineChanged     -> form = form.copy(deadline = action.v)
            is GigFormAction.LocationSelected    -> form = form.copy(location = action.v)
            is GigFormAction.BudgetTypeSelected  -> form = form.copy(budgetType = action.v)
            is GigFormAction.BudgetAmountChanged -> form = form.copy(budgetAmount = action.v.filter { it.isDigit() })
            is GigFormAction.BudgetRangeSelected -> form = form.copy(budgetAmount = action.midpoint)
            GigFormAction.ToggleTerms            -> form = form.copy(termsAccepted = !form.termsAccepted)
            is GigFormAction.PostGig -> {
                val err = validateStep(3, form)
                if (err.isNotEmpty()) {
                    form = form.copy(stepError = err)
                } else {
                    action.onPost(
                        Gig(
                            title       = form.title.trim(),
                            description = form.description.trim(),
                            category    = form.category,
                            budget      = form.budgetAmount.toDoubleOrNull() ?: 0.0,
                            duration    = if (form.budgetType == "Hourly Rate") "Hourly" else "Fixed",
                            location    = form.location,
                            skills      = form.skills,
                            deadline    = form.deadline.trim()
                        )
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // ── Top Bar ──────────────────────────────────────────────────────────
        WizardTopBar(
            step   = form.step,
            onBack = { onAction(GigFormAction.PrevStep) }
        )

        // ── Step Progress ────────────────────────────────────────────────────
        StepProgressBar(
            currentStep = form.step,
            labels      = STEP_LABELS,
            modifier    = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )

        // ── Animated Step Content ────────────────────────────────────────────
        AnimatedContent(
            targetState   = form.step,
            modifier      = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            transitionSpec = {
                val forward = targetState > initialState
                (slideInHorizontally(tween(260)) { if (forward) it else -it } + fadeIn(tween(260))) togetherWith
                (slideOutHorizontally(tween(220)) { if (forward) -it else it } + fadeOut(tween(220)))
            },
            label = "step_content"
        ) { step ->
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                when (step) {
                    0 -> Step1Basics(
                        form     = form,
                        onAction = onAction
                    )
                    1 -> Step2Details(
                        form     = form,
                        onAction = onAction
                    )
                    2 -> Step3Budget(
                        form     = form,
                        onAction = onAction
                    )
                    3 -> Step4Review(
                        form        = form,
                        createState = createState,
                        onAction    = onAction,
                        onPost      = { viewModel.createGig(it) }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Step Error Banner ────────────────────────────────────────────────
        if (form.stepError.isNotBlank()) {
            StepErrorBanner(message = form.stepError)
        }

        // ── Bottom Nav (Back / Continue) — hidden on Step 4 which has its own CTA
        if (form.step < 3) {
            WizardBottomNav(
                step         = form.step,
                onBack       = { onAction(GigFormAction.PrevStep) },
                onContinue   = { onAction(GigFormAction.NextStep) }
            )
        }
    }
}

// ─── Wizard Top Bar ───────────────────────────────────────────────────────────

@Composable
private fun WizardTopBar(step: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(0.dp))
            .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
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
                "Post a Gig",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 18.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Step ${step + 1} of ${STEP_LABELS.size} — ${STEP_LABELS[step]}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// ─── Step Progress Bar ────────────────────────────────────────────────────────

@Composable
private fun StepProgressBar(
    currentStep: Int,
    labels:      List<String>,
    modifier:    Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Segment track
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            labels.forEachIndexed { index, _ ->
                val bg = when {
                    index < currentStep  -> MaterialTheme.colorScheme.primary
                    index == currentStep -> MaterialTheme.colorScheme.primary  // active segment gets gradient via Brush
                    else                 -> MaterialTheme.colorScheme.outlineVariant
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index == currentStep)
                                Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                            else
                                Brush.linearGradient(listOf(bg, bg))
                        )
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Labels row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            labels.forEachIndexed { index, label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize   = 10.sp,
                            fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = when {
                            index < currentStep  -> MaterialTheme.colorScheme.primary
                            index == currentStep -> MaterialTheme.colorScheme.onBackground
                            else                 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }
            }
        }
    }
}

// ─── STEP 1: Basics ──────────────────────────────────────────────────────────

@Composable
private fun Step1Basics(
    form:     GigFormState,
    onAction: (GigFormAction) -> Unit
) {
    StepHeading(
        title    = "Tell us about your gig",
        subtitle = "Choose a category, then describe what you need"
    )

    // Category grid — 2 columns
    FormLabel("Category *")
    Spacer(Modifier.height(10.dp))
    GIG_CATEGORY_ITEMS.chunked(2).forEach { row ->
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { cat ->
                val isSelected = form.category == cat.name
                val borderColor by animateColorAsState(
                    targetValue   = if (isSelected) cat.color else Color.Transparent,
                    animationSpec = tween(160),
                    label         = "cat_border_${cat.name}"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cat.color.copy(alpha = if (isSelected) 0.18f else 0.08f))
                        .border(
                            BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { onAction(GigFormAction.CategorySelected(cat.name)) }
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(cat.emoji, fontSize = 22.sp)
                        Text(
                            cat.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize   = 12.sp
                            ),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Selection checkmark
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .indigoGradient(radius = 10),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(11.dp))
                        }
                    }
                }
            }
            // Fill last row if odd number of categories
            if (row.size < 2) Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
    }

    Spacer(Modifier.height(20.dp))

    // Gig Title
    FormLabel("Gig Title *")
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value         = form.title,
        onValueChange = { onAction(GigFormAction.TitleChanged(it)) },
        placeholder   = {
            Text(
                "e.g. Need a React developer for my startup dashboard",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f).copy(alpha = 0.55f),
                style = MaterialTheme.typography.bodySmall
            )
        },
        singleLine      = true,
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(12.dp),
        isError         = form.title.isNotBlank() && form.title.length < 10,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        supportingText  = {
            Text(
                "${form.title.length} / 100",
                color = if (form.title.length >= 10) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else SemanticWarning,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
            )
        },
        colors = gigFieldColors()
    )

    Spacer(Modifier.height(14.dp))

    // Description
    FormLabel("Description *")
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value         = form.description,
        onValueChange = { onAction(GigFormAction.DescriptionChanged(it)) },
        placeholder   = {
            Text(
                "Describe exactly what you need, key requirements, and what success looks like…",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f).copy(alpha = 0.55f),
                style = MaterialTheme.typography.bodySmall
            )
        },
        modifier       = Modifier
            .fillMaxWidth()
            .height(160.dp),
        maxLines       = 10,
        shape          = RoundedCornerShape(12.dp),
        supportingText = {
            Text(
                "${form.description.length} / 2000",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
            )
        },
        colors = gigFieldColors()
    )

    // Pro tip card
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SemanticWarningBg)
            .border(BorderStroke(1.dp, SemanticWarning.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Text("💡", fontSize = 14.sp)
        Text(
            "Specific descriptions with clear deliverables get 2× more quality applications",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = SemanticWarning
        )
    }
}

// ─── STEP 2: Details ─────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step2Details(
    form:     GigFormState,
    onAction: (GigFormAction) -> Unit
) {
    val focusManager = LocalFocusManager.current

    StepHeading(
        title    = "Additional Details",
        subtitle = "Skills required, deadline, and work location"
    )

    // Skills tag input
    FormLabel("Required Skills")
    Spacer(Modifier.height(6.dp))
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value         = form.skillInput,
            onValueChange = { onAction(GigFormAction.SkillInputChanged(it)) },
            placeholder   = {
                Text(
                    "e.g. Kotlin, Figma, React",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f).copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodySmall
                )
            },
            singleLine      = true,
            modifier        = Modifier.weight(1f),
            shape           = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onAction(GigFormAction.AddSkill)
                focusManager.clearFocus()
            }),
            colors = gigFieldColors()
        )
        Box(
            modifier = Modifier
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .indigoGradient(radius = 12)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null
                ) {
                    onAction(GigFormAction.AddSkill)
                    focusManager.clearFocus()
                }
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Add",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }

    // Skill pills
    if (form.skills.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement   = Arrangement.spacedBy(6.dp)
        ) {
            form.skills.forEach { skill ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(GlowIndigo)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)), RoundedCornerShape(7.dp))
                        .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        skill,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = null
                            ) { onAction(GigFormAction.RemoveSkill(skill)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(9.dp))
                    }
                }
            }
        }
    }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onAction(GigFormAction.DeadlineChanged(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }
    }

    Spacer(Modifier.height(22.dp))

    // Deadline
    FormLabel("Application Deadline")
    Spacer(Modifier.height(6.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { datePickerDialog.show() }
    ) {
        OutlinedTextField(
            value = form.deadline,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            placeholder = {
                Text(
                    "Tap to select deadline from calendar… 📅",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Select Date from Calendar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { datePickerDialog.show() }
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
            )
        )
    }

    Spacer(Modifier.height(22.dp))

    // Location type picker
    FormLabel("How will work be done?")
    Spacer(Modifier.height(10.dp))
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LOCATION_OPTIONS.forEach { (type, emoji, desc) ->
            val isSelected = form.location == type
            val borderColor by animateColorAsState(
                targetValue   = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                animationSpec = tween(140),
                label         = "loc_border_$type"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) GlowIndigo else MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onAction(GigFormAction.LocationSelected(type)) }
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(emoji, fontSize = 20.sp)
                    Text(
                        type,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize   = 11.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        desc,
                        style   = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color   = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ─── STEP 3: Budget ───────────────────────────────────────────────────────────

@Composable
private fun Step3Budget(
    form:     GigFormState,
    onAction: (GigFormAction) -> Unit
) {
    StepHeading(
        title    = "Set Your Budget",
        subtitle = "How much will you pay for this work?"
    )

    // Fixed / Hourly pill toggle
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        listOf("Fixed Price", "Hourly Rate").forEach { type ->
            val isSel = form.budgetType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        if (isSel)
                            Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                        else
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onAction(GigFormAction.BudgetTypeSelected(type)) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    type,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(Modifier.height(32.dp))

    // Large underline amount input — centred, hero treatment
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Enter amount",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize      = 11.sp,
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "₹",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 28.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(Modifier.width(2.dp))
            // Transparent field — only the bottom border shows
            Box(
                modifier = Modifier
                    .width(200.dp)
            ) {
                OutlinedTextField(
                    value         = form.budgetAmount,
                    onValueChange = { onAction(GigFormAction.BudgetAmountChanged(it)) },
                    placeholder   = {
                        Text(
                            "0",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 44.sp
                            ),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 44.sp,
                        color      = MaterialTheme.colorScheme.onBackground
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor      = Color.Transparent,
                        unfocusedBorderColor    = Color.Transparent,
                        cursorColor             = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                // Manual bottom underline
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.85f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (form.budgetAmount.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
            if (form.budgetType == "Hourly Rate") {
                Text(
                    "/hr",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }

    Spacer(Modifier.height(28.dp))

    // Quick range chips
    Text(
        "SUGGESTED RANGES",
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
    Spacer(Modifier.height(8.dp))
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BUDGET_RANGE_CHIPS.forEach { (label, midpoint) ->
            val isActive = form.budgetAmount == midpoint
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) GlowIndigo else MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onAction(GigFormAction.BudgetRangeSelected(midpoint)) }
                    .padding(horizontal = 9.dp, vertical = 7.dp)
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize   = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(Modifier.height(18.dp))

    // Market rate info card
    val rateInfo = MARKET_RATES[form.category] ?: "₹300–₹3,000"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Text("📊", fontSize = 16.sp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "Market rate for ${form.category.ifBlank { "this category" }} gigs",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 12.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                rateInfo,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = SemanticSuccess
            )
        }
    }
}

// ─── STEP 4: Review ───────────────────────────────────────────────────────────

@Composable
private fun Step4Review(
    form:        GigFormState,
    createState: Resource<Gig>?,
    onAction:    (GigFormAction) -> Unit,
    onPost:      (Gig) -> Unit
) {
    StepHeading(
        title    = "Looks good?",
        subtitle = "Review your gig before it goes live"
    )

    // Checklist
    Text(
        "DETAILS CHECKLIST",
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
    Spacer(Modifier.height(10.dp))

    val checklistItems = listOf(
        Triple("Category",    form.category.ifBlank { "Not selected" },                        form.category.isNotBlank()),
        Triple("Title",       if (form.title.length >= 10) "${form.title.length}/100 chars" else "Too short (min 10)", form.title.length >= 10),
        Triple("Description", if (form.description.isNotBlank()) "${form.description.length} chars" else "Missing",     form.description.isNotBlank()),
        Triple("Skills",      if (form.skills.isNotEmpty()) "${form.skills.size} added" else "Optional — none added",   true),
        Triple("Budget",      if (form.budgetAmount.isNotBlank()) "₹${form.budgetAmount} (${form.budgetType})" else "Missing", form.budgetAmount.isNotBlank()),
        Triple("Deadline",    form.deadline.ifBlank { "Not specified (optional)" },            true),
        Triple("Location",    form.location,                                                   true),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .formCard()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        checklistItems.forEachIndexed { i, (label, value, ok) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status icon
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (ok) SemanticSuccessBg else SemanticErrorBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (ok) Icons.Default.Check else Icons.Default.Close,
                        null,
                        tint     = if (ok) SemanticSuccess else SemanticError,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 10.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = if (ok) MaterialTheme.colorScheme.onSurfaceVariant else SemanticError
                    )
                }
            }
            // Hairline divider except after last item
            if (i < checklistItems.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    // Terms checkbox
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { onAction(GigFormAction.ToggleTerms) }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom checkbox
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (form.termsAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(
                    BorderStroke(1.5.dp, if (form.termsAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    RoundedCornerShape(5.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (form.termsAccepted) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "I confirm this gig is genuine and follows CampusGig community guidelines",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // API error from server
    if (createState is Resource.Error) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SemanticErrorBg)
                .border(BorderStroke(1.dp, SemanticError.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("⚠️", fontSize = 14.sp)
            Text(
                (createState as Resource.Error).message,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = SemanticError
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    // Post Gig CTA
    val canPost = form.termsAccepted && createState !is Resource.Loading
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (form.termsAccepted)
                    Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoMid, GradientIndigoEnd))
                else
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.surfaceContainerHigh))
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                enabled           = canPost
            ) {
                onAction(GigFormAction.PostGig { gig -> onPost(gig) })
            },
        contentAlignment = Alignment.Center
    ) {
        if (createState is Resource.Loading) {
            CircularProgressIndicator(
                color       = Color.White,
                modifier    = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.RocketLaunch, null,
                    tint     = if (form.termsAccepted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Post Gig",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (form.termsAccepted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ─── Wizard Bottom Nav ────────────────────────────────────────────────────────

@Composable
private fun WizardBottomNav(
    step:       Int,
    onBack:     () -> Unit,
    onContinue: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(0.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (step > 0) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .formCard(radius = 26)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Back",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(if (step > 0) 2f else 1f)
                .height(52.dp)
                .indigoGradient(radius = 26)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onContinue
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Continue →",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

// ─── Step Error Banner ────────────────────────────────────────────────────────

@Composable
private fun StepErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SemanticErrorBg)
            .border(BorderStroke(1.dp, SemanticError.copy(alpha = 0.25f)), RoundedCornerShape(0.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text("⚠️", fontSize = 13.sp)
        Text(
            message,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = SemanticError
        )
    }
}

// ─── Shared Composables ───────────────────────────────────────────────────────

@Composable
private fun StepHeading(title: String, subtitle: String) {
    Text(
        title,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 22.sp
        ),
        color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(Modifier.height(4.dp))
    Text(
        subtitle,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
    Spacer(Modifier.height(22.dp))
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight    = FontWeight.Bold,
            fontSize      = 12.sp,
            letterSpacing = 0.4.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
