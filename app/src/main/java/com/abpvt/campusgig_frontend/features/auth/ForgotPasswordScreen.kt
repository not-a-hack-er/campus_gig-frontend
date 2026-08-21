/**
 * ForgotPasswordScreen.kt — 3-Step Password Reset Flow
 *
 * Implements:
 *  Step 1: Enter email -> calls POST /api/auth/forgot-password
 *  Step 2: Enter 6-digit OTP -> calls POST /api/auth/verify-otp (gets resetToken)
 *  Step 3: Enter new password -> calls POST /api/auth/reset-password
 *  Step 4: Success confirmation & back to Login
 */
package com.abpvt.campusgig_frontend.features.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ForgotPasswordViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.GlowIndigo
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticErrorBg
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccessBg
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).authRepository
        )
    )
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val emailValue by viewModel.email.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var emailInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Resend countdown timer
    var resendCooldown by remember { mutableIntStateOf(60) }
    LaunchedEffect(currentStep) {
        if (currentStep == ForgotPasswordStep.VERIFY_OTP) {
            resendCooldown = 60
            while (resendCooldown > 0) {
                delay(1000)
                resendCooldown -= 1
            }
        }
    }

    val focusManager = LocalFocusManager.current
    val isLoading = actionState is Resource.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top ambient glow
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(GlowIndigo.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // ── Top Navigation Bar ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable {
                            when (currentStep) {
                                ForgotPasswordStep.ENTER_EMAIL, ForgotPasswordStep.SUCCESS -> navController.popBackStack()
                                ForgotPasswordStep.VERIFY_OTP -> viewModel.goToStep(ForgotPasswordStep.ENTER_EMAIL)
                                ForgotPasswordStep.NEW_PASSWORD -> viewModel.goToStep(ForgotPasswordStep.VERIFY_OTP)
                            }
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

                // Step Indicator Pills
                if (currentStep != ForgotPasswordStep.SUCCESS) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepPill(step = 1, isActive = currentStep == ForgotPasswordStep.ENTER_EMAIL, isCompleted = currentStep.ordinal > 0)
                        StepDivider(isCompleted = currentStep.ordinal > 0)
                        StepPill(step = 2, isActive = currentStep == ForgotPasswordStep.VERIFY_OTP, isCompleted = currentStep.ordinal > 1)
                        StepDivider(isCompleted = currentStep.ordinal > 1)
                        StepPill(step = 3, isActive = currentStep == ForgotPasswordStep.NEW_PASSWORD, isCompleted = currentStep == ForgotPasswordStep.SUCCESS)
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Error Banner ───────────────────────────────────────────────────
            if (actionState is Resource.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SemanticErrorBg, RoundedCornerShape(10.dp))
                        .border(1.dp, SemanticError.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = (actionState as Resource.Error).message,
                        style = MaterialTheme.typography.bodySmall,
                        color = SemanticError
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Dynamic Content Per Step ───────────────────────────────────────
            when (currentStep) {
                ForgotPasswordStep.ENTER_EMAIL -> {
                    // Header
                    HeroHeader(
                        icon = "🔑",
                        title = "Forgot Password?",
                        subtitle = "Enter your registered college or personal email address to receive a 6-digit recovery code."
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    CampusInputField(
                        label = "Email Address",
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = "yourname@college.edu",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (emailInput.isNotBlank()) viewModel.requestOtp(emailInput)
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        text = "Send Recovery Code",
                        isLoading = isLoading,
                        enabled = emailInput.isNotBlank() && !isLoading,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.requestOtp(emailInput)
                        }
                    )
                }

                ForgotPasswordStep.VERIFY_OTP -> {
                    HeroHeader(
                        icon = "✉️",
                        title = "Verify Code",
                        subtitle = "Enter the 6-digit verification code sent to:\n$emailValue"
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    CampusInputField(
                        label = "6-Digit OTP Code",
                        value = otpInput,
                        onValueChange = { if (it.length <= 6) otpInput = it },
                        placeholder = "e.g. 123456",
                        leadingIcon = Icons.Default.Password,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (otpInput.length == 6) viewModel.verifyOtp(otpInput)
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resend OTP Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.goToStep(ForgotPasswordStep.ENTER_EMAIL) }
                        ) {
                            Text("Change Email", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }

                        if (resendCooldown > 0) {
                            Text(
                                "Resend in ${resendCooldown}s",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    viewModel.resendOtp()
                                    resendCooldown = 60
                                }
                            ) {
                                Text("Resend Code", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = "Verify & Continue",
                        isLoading = isLoading,
                        enabled = otpInput.trim().length >= 4 && !isLoading,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.verifyOtp(otpInput)
                        }
                    )
                }

                ForgotPasswordStep.NEW_PASSWORD -> {
                    HeroHeader(
                        icon = "🔒",
                        title = "Set New Password",
                        subtitle = "Create a new strong password for your CampusGig account."
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    CampusInputField(
                        label = "New Password",
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = "At least 6 characters",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CampusInputField(
                        label = "Confirm New Password",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Re-enter new password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (newPassword.length >= 6 && newPassword == confirmPassword) {
                                    viewModel.resetPassword(newPassword)
                                }
                            }
                        )
                    )

                    if (confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Passwords do not match", color = SemanticError, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val passwordsValid = newPassword.length >= 6 && newPassword == confirmPassword
                    PrimaryButton(
                        text = "Reset Password",
                        isLoading = isLoading,
                        enabled = passwordsValid && !isLoading,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.resetPassword(newPassword)
                        }
                    )
                }

                ForgotPasswordStep.SUCCESS -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(SemanticSuccessBg)
                                .border(1.5.dp, SemanticSuccess, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SemanticSuccess,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Password Reset Complete! 🎉",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your password has been updated successfully. You can now log in to CampusGig with your new credentials.",
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        PrimaryButton(
                            text = "Back to Sign In",
                            isLoading = false,
                            enabled = true,
                            onClick = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ─── Sub-Composables ──────────────────────────────────────────────────────────

@Composable
private fun HeroHeader(icon: String, title: String, subtitle: String) {
    Column {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientIndigoStart, GradientIndigoEnd)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val buttonInteraction = remember { MutableInteractionSource() }
    val isPressed by buttonInteraction.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (enabled)
                    Brush.linearGradient(colors = listOf(GradientIndigoStart, GradientIndigoEnd))
                else
                    Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
            )
            .clickable(enabled = enabled) { onClick() },
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
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StepPill(step: Int, isActive: Boolean, isCompleted: Boolean) {
    val bg = when {
        isCompleted -> SemanticSuccess
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isActive || isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isCompleted) "✓" else step.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun StepDivider(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(16.dp)
            .height(2.dp)
            .background(if (isCompleted) SemanticSuccess else MaterialTheme.colorScheme.outline)
    )
}
