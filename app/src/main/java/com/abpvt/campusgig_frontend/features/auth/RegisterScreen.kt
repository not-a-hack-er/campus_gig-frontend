/**
 * RegisterScreen.kt — v2.0 Multi-Step Registration
 *
 * CONCEPT: 2-step registration so it never feels overwhelming.
 * Step 1: Identity (Name, Email, Password + strength, Confirm)
 * Step 2: Campus Profile (College picker, Course, Year chips, Skills tags)
 *
 * FEATURES:
 * - Animated step progress indicator at top (gradient fill to current)
 * - Password strength meter (4-segment: Weak/Fair/Good/Strong)
 * - Year of study chip selector (horizontal, single select, gradient active state)
 * - Skills tag input (type + add, shown as gradient-border removable chips)
 * - College bottom sheet with search + alphabetical list
 * - Staggered step transition animations
 * - Gradient CTA button per step
 */
package com.abpvt.campusgig_frontend.features.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.AuthViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Constants
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.GlowIndigo
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticErrorBg
import com.abpvt.campusgig_frontend.ui.theme.SemanticInfo
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.ui.theme.Violet400
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).authRepository
        )
    )
) {
    // ── Step 1 State ──────────────────────────────────────────────────────────
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // ── Step 2 State ──────────────────────────────────────────────────────────
    var college by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    val skills = remember { mutableStateListOf<String>() }
    var skillInput by remember { mutableStateOf("") }
    var showCollegePicker by remember { mutableStateOf(false) }
    var collegeSearch by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    // ── Global State ──────────────────────────────────────────────────────────
    var currentStep by remember { mutableStateOf(1) }
    var localError by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val role = Constants.ROLE_STUDENT
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(authState) {
        if (authState is Resource.Success) {
            viewModel.resetState()
            navController.navigate(Routes.HOME) {
                popUpTo(Routes.REGISTER) { inclusive = true }
            }
        }
    }

    // Popular colleges list
    val popularColleges = remember {
        listOf(
            "IIT Bombay", "IIT Delhi", "IIT Madras", "IIT Kanpur", "IIT Kharagpur",
            "BITS Pilani", "BITS Goa", "BITS Hyderabad",
            "NIT Trichy", "NIT Warangal", "NIT Surathkal",
            "VIT Vellore", "VIT Chennai", "Manipal Institute of Technology",
            "SRM Institute of Science and Technology", "Amity University",
            "Delhi University", "Mumbai University", "Pune University",
            "Anna University", "Osmania University", "Jadavpur University",
            "IIIT Hyderabad", "IIIT Delhi", "IIIT Allahabad",
            "Christ University", "Symbiosis International University",
            "Lovely Professional University", "Chandigarh University"
        ).sorted()
    }

    val yearOptions = listOf("1st", "2nd", "3rd", "4th", "5th+")

    // Password strength
    val passwordStrength = remember(password) {
        when {
            password.length < 6 -> 0
            password.length < 8 -> 1
            password.length < 10 && !password.any { it.isDigit() } -> 2
            password.any { it.isDigit() } && password.any { it.isUpperCase() } && password.length >= 10 -> 4
            else -> 3
        }
    }
    val strengthLabels = listOf("", "Weak", "Fair", "Good", "Strong")
    val strengthColors = listOf(
        Color.Transparent,
        SemanticError,
        SemanticWarning,
        SemanticInfo,
        SemanticSuccess
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(GlowIndigo, Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // ── Back Button ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable {
                        if (currentStep > 1) currentStep-- else navController.popBackStack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Step Progress Indicator ─────────────────────────────────────────
            StepProgressIndicator(
                currentStep = currentStep,
                totalSteps = 2,
                stepLabels = listOf("Account", "Campus")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Animated Step Content ──────────────────────────────────────────
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300))
                    } else {
                        slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
                    }
                },
                label = "stepContent"
            ) { step ->

                when (step) {
                    // ════════════════════════════════════════════════════════
                    // STEP 1 — Create Your Account
                    // ════════════════════════════════════════════════════════
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text(
                                    text = "Create Your Account",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Join 10,000+ students already earning",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // First Name + Last Name side by side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CampusInputField(
                                    label = "First Name",
                                    value = firstName,
                                    onValueChange = { firstName = it },
                                    placeholder = "Akarsh",
                                    leadingIcon = Icons.Default.Person,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Next
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                CampusInputField(
                                    label = "Last Name",
                                    value = lastName,
                                    onValueChange = { lastName = it },
                                    placeholder = "Bajpai",
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        imeAction = ImeAction.Next
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Email
                            CampusInputField(
                                label = "Email",
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "you@college.edu",
                                leadingIcon = Icons.Default.Email,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                )
                            )

                            // Password + strength meter
                            Column {
                                CampusInputField(
                                    label = "Password",
                                    value = password,
                                    onValueChange = { password = it },
                                    placeholder = "Min 8 characters",
                                    leadingIcon = Icons.Default.Lock,
                                    isPassword = true,
                                    passwordVisible = passwordVisible,
                                    onPasswordToggle = { passwordVisible = !passwordVisible },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Next
                                    )
                                )
                                if (password.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    PasswordStrengthIndicator(
                                        strength = passwordStrength,
                                        label = strengthLabels.getOrElse(passwordStrength) { "" },
                                        color = strengthColors.getOrElse(passwordStrength) { Color.Transparent }
                                    )
                                }
                            }

                            // Confirm Password
                            CampusInputField(
                                label = "Confirm Password",
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = "Re-enter your password",
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = confirmPasswordVisible,
                                onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                                isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                                errorMessage = "Passwords don't match",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                )
                            )

                            // Local Error
                            if (localError.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SemanticErrorBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, SemanticError.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = localError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SemanticError
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Continue button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(GradientIndigoStart, GradientIndigoEnd)
                                        )
                                    )
                                    .clickable {
                                        localError = when {
                                            firstName.isBlank() -> "Please enter your first name"
                                            lastName.isBlank() -> "Please enter your last name"
                                            email.isBlank() -> "Please enter your email"
                                            !email.contains("@") -> "Enter a valid email address"
                                            password.length < 6 -> "Password must be at least 6 characters"
                                            password != confirmPassword -> "Passwords don't match"
                                            else -> ""
                                        }
                                        if (localError.isEmpty()) {
                                            currentStep = 2
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Continue →",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White
                                )
                            }

                            // Already have account
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Already have an account? ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Sign in",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { navController.popBackStack() }
                                )
                            }
                        }
                    }

                    // ════════════════════════════════════════════════════════
                    // STEP 2 — Your Campus Profile
                    // ════════════════════════════════════════════════════════
                    2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text(
                                    text = "Tell Us About You",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Helps you find gigs that match your skills",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // College Picker (tap to open bottom sheet)
                            Column {
                                Text(
                                    text = "College / University",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                        .clickable { showCollegePicker = true }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = if (college.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = college.ifBlank { "Select your college" },
                                            style = androidx.compose.ui.text.TextStyle(
                                                fontSize = 16.sp,
                                                color = if (college.isNotBlank()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                }
                            }

                            // Course
                            CampusInputField(
                                label = "Course / Program",
                                value = course,
                                onValueChange = { course = it },
                                placeholder = "e.g. Computer Science",
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                )
                            )

                            // Year of Study chips
                            Column {
                                Text(
                                    text = "Year of Study",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    yearOptions.forEach { year ->
                                        val isSelected = selectedYear == year
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) Brush.linearGradient(
                                                        colors = listOf(
                                                            GradientIndigoStart.copy(alpha = 0.2f),
                                                            GradientIndigoEnd.copy(alpha = 0.2f)
                                                        )
                                                    ) else Brush.linearGradient(
                                                        colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                                                    )
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedYear = year }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = year,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Skills tag input
                            Column {
                                Text(
                                    text = "Skills (optional)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                // Skill chips
                                if (skills.isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    ) {
                                        skills.forEach { skill ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(GradientIndigoStart.copy(alpha = 0.12f))
                                                    .border(
                                                        1.dp,
                                                        Brush.linearGradient(
                                                            colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), Violet400.copy(alpha = 0.5f))
                                                        ),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = skill,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Medium
                                                        ),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Remove $skill",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable { skills.remove(skill) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                // Skill text input
                                CampusInputField(
                                    label = "",
                                    value = skillInput,
                                    onValueChange = { skillInput = it },
                                    placeholder = "Type a skill and press space (e.g. Kotlin)",
                                    keyboardOptions = KeyboardOptions(
                                        imeAction = ImeAction.Done,
                                        capitalization = KeyboardCapitalization.Words
                                    ),
                                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                        onDone = {
                                            val s = skillInput.trim()
                                            if (s.isNotBlank() && !skills.contains(s)) {
                                                skills.add(s)
                                            }
                                            skillInput = ""
                                        }
                                    )
                                )
                                Text(
                                    text = "Press Done on keyboard to add each skill",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Terms checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (agreedToTerms) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            1.dp,
                                            if (agreedToTerms) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { agreedToTerms = !agreedToTerms },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (agreedToTerms) {
                                        Text("✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "I agree to the Terms of Service and Privacy Policy",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // API Error
                            if (authState is Resource.Error) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SemanticErrorBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, SemanticError.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = (authState as Resource.Error).message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SemanticError
                                    )
                                }
                            }
                            if (localError.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SemanticErrorBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, SemanticError.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = localError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SemanticError
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Create Account CTA
                            val isLoading = authState is Resource.Loading
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (!isLoading)
                                            Brush.linearGradient(
                                                colors = listOf(GradientIndigoStart, GradientIndigoEnd)
                                            )
                                        else
                                            Brush.linearGradient(
                                                colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                    )
                                    .clickable(enabled = !isLoading) {
                                        localError = when {
                                            college.isBlank() -> "Please select your college"
                                            !agreedToTerms -> "Please agree to the Terms of Service"
                                            else -> ""
                                        }
                                        if (localError.isEmpty()) {
                                            val fullName = "$firstName $lastName".trim()
                                            viewModel.register(
                                                name        = fullName,
                                                email       = email,
                                                password    = password,
                                                role        = role,
                                                college     = college,
                                                branch      = course,
                                                yearOfStudy = selectedYear,
                                                skills      = skills.toList()
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Create Account 🎉",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }

                            // Sign in link
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Already have an account? ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Sign in",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ── College Picker Bottom Sheet ────────────────────────────────────────────
    if (showCollegePicker) {
        ModalBottomSheet(
            onDismissRequest = { showCollegePicker = false },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Select Your College",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Search bar
                CampusInputField(
                    label = "",
                    value = collegeSearch,
                    onValueChange = { collegeSearch = it },
                    placeholder = "Search colleges...",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val filtered = popularColleges.filter {
                    collegeSearch.isBlank() || it.contains(collegeSearch, ignoreCase = true)
                }

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    filtered.forEach { c ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (college == c) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable {
                                    college = c
                                    collegeSearch = ""
                                    scope.launch { bottomSheetState.hide() }
                                    showCollegePicker = false
                                }
                                .padding(horizontal = 12.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = c,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (college == c) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// ─── Step Progress Indicator ──────────────────────────────────────────────────
@Composable
private fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepLabels: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1
            val isCompleted = stepNumber < currentStep
            val isActive = stepNumber == currentStep

            // Step circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive || isCompleted)
                            Brush.linearGradient(
                                colors = listOf(GradientIndigoStart, GradientIndigoEnd)
                            )
                        else
                            Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                    )
                    .border(
                        1.dp,
                        if (isActive || isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCompleted) "✓" else stepNumber.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isActive || isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = stepLabels.getOrElse(index) { "" },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 13.sp
            )

            // Connecting line
            if (index < totalSteps - 1) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (currentStep > stepNumber)
                                Brush.horizontalGradient(
                                    colors = listOf(GradientIndigoStart, GradientIndigoEnd)
                                )
                            else
                                Brush.horizontalGradient(colors = listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

// ─── Password Strength Indicator ─────────────────────────────────────────────
@Composable
private fun PasswordStrengthIndicator(
    strength: Int,        // 1–4
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(4) { index ->
                val filled = index < strength
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (filled) color else MaterialTheme.colorScheme.outline)
                )
            }
        }
        if (label.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

// ─── LocalContext import (needed inside the composable) ───────────────────────
private val LocalContext get() = androidx.compose.ui.platform.LocalContext
