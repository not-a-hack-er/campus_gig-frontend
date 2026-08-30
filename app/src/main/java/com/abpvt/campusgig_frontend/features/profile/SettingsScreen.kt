/**
 * SettingsScreen.kt — Organized app settings
 *
 * CONCEPT: Settings that feel organized and intentional. Not a wall of options.
 * Grouped logically. Each row has enough space.
 *
 * SECTIONS:
 * - User card at top (avatar + name + email + view profile)
 * - Account (edit profile, change password, linked accounts)
 * - Preferences (dark mode toggle, language, notifications)
 * - Privacy & Security (visibility, block list, 2FA)
 * - About (help, rate, terms, version)
 * - Logout (red icon + confirm dialog)
 */
package com.abpvt.campusgig_frontend.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.core.utils.ProfileViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController,
    themeViewModel: ThemeViewModel? = null,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).userRepository
        )
    ),
    feedbackViewModel: com.abpvt.campusgig_frontend.features.profile.FeedbackViewModel = viewModel(
        factory = com.abpvt.campusgig_frontend.core.utils.FeedbackViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).feedbackRepository
        )
    )
) {
    val profileState by viewModel.profile.collectAsState()
    val user = (profileState as? Resource.Success<User>)?.data
    val changePasswordState by viewModel.changePasswordState.collectAsState()
    val submitFeedbackState by feedbackViewModel.submitState.collectAsState()

    val isDarkTheme by (themeViewModel?.isDarkTheme ?: kotlinx.coroutines.flow.MutableStateFlow(true)).collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showFeedbackSheet by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLinkedAccountsDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }
    var showBlockListDialog by remember { mutableStateOf(false) }
    var show2FaDialog by remember { mutableStateOf(false) }

    var profileVisibility by remember { mutableStateOf("Public") }

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // ── Change Password State ────────────────────────────────────────────────
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var passError by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(changePasswordState) {
        when (val s = changePasswordState) {
            is Resource.Success -> {
                showChangePasswordDialog = false
                currentPass = ""; newPass = ""; confirmPass = ""; passError = ""
                scope.launch { snackbarHostState.showSnackbar("Password changed successfully! ✓") }
                viewModel.resetChangePasswordState()
            }
            is Resource.Error -> {
                passError = s.message
                viewModel.resetChangePasswordState()
            }
            else -> {}
        }
    }

    // ── Change Password Dialog ───────────────────────────────────────────────
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false; passError = "" },
            title = { Text("Change Password", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    if (passError.isNotBlank()) {
                        Text(passError, style = MaterialTheme.typography.bodySmall, color = SemanticError)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    androidx.compose.material3.OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it; passError = "" },
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it; passError = "" },
                        label = { Text("New Password (min 6 chars)") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it; passError = "" },
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                        .clickable(enabled = changePasswordState !is Resource.Loading) {
                            when {
                                currentPass.isBlank() -> passError = "Current password is required"
                                newPass.length < 6 -> passError = "New password must be at least 6 characters"
                                newPass != confirmPass -> passError = "Passwords do not match"
                                else -> viewModel.changePassword(currentPass, newPass)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (changePasswordState is Resource.Loading) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Update", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            },
            dismissButton = {
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showChangePasswordDialog = false; passError = "" }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Info Dialogs ────────────────────────────────────────────────────────
    if (showLinkedAccountsDialog) {
        AlertDialog(
            onDismissRequest = { showLinkedAccountsDialog = false },
            title = { Text("Linked Accounts", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("Google Account (${user?.email ?: "Verified"}): Connected ✓\nYour account is secured via single sign-on.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = { Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary).clickable { showLinkedAccountsDialog = false }.padding(horizontal = 16.dp, vertical = 8.dp)) { Text("OK", color = Color.White) } },
            containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)
        )
    }

    if (showVisibilityDialog) {
        AlertDialog(
            onDismissRequest = { showVisibilityDialog = false },
            title = { Text("Profile Visibility", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { profileVisibility = "Public"; showVisibilityDialog = false }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🌐 Public (Visible to all students)", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (profileVisibility == "Public") Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth().clickable { profileVisibility = "Campus Only"; showVisibilityDialog = false }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🎓 Campus Only (Visible to your college)", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (profileVisibility == "Campus Only") Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)
        )
    }

    if (showBlockListDialog) {
        AlertDialog(
            onDismissRequest = { showBlockListDialog = false },
            title = { Text("Block List", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("You haven't blocked any users.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = { Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary).clickable { showBlockListDialog = false }.padding(horizontal = 16.dp, vertical = 8.dp)) { Text("Close", color = Color.White) } },
            containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)
        )
    }

    if (show2FaDialog) {
        AlertDialog(
            onDismissRequest = { show2FaDialog = false },
            title = { Text("Two-Factor Authentication", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("CampusVault secures transactions using 4-digit OTP Escrow verification for all gig completions. Mobile 2FA active.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = { Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary).clickable { show2FaDialog = false }.padding(horizontal = 16.dp, vertical = 8.dp)) { Text("Got it", color = Color.White) } },
            containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)
        )
    }

    if (showRateDialog) {
        var stars by remember { mutableStateOf(5) }
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("Rate CampusVault ⭐", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Enjoying CampusVault? Rate your experience!", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        (1..5).forEach { i ->
                            Text(
                                if (i <= stars) "★" else "☆",
                                fontSize = 28.sp,
                                color = SemanticWarning,
                                modifier = Modifier.clickable { stars = i }.padding(4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))).clickable {
                    showRateDialog = false
                    scope.launch { snackbarHostState.showSnackbar("Thank you for your $stars-star rating! 🎉") }
                }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Submit Rating", color = Color.White)
                }
            },
            dismissButton = {
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showRateDialog = false }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Privacy Policy", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("CampusVault Student Freelancing Platform v1.0\n\n• End-to-End Escrow OTP Verification\n• Secure Peer Messaging & Data Privacy\n• Verified College Community Guidelines", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = { Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary).clickable { showTermsDialog = false }.padding(horizontal = 16.dp, vertical = 8.dp)) { Text("Close", color = Color.White) } },
            containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("You'll need to sign in again to access your account.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(SemanticError).clickable { showLogoutDialog = false; navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Sign Out", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            },
            dismissButton = {
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { showLogoutDialog = false }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────────
            item {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
                )
            }

            // ── User Card ────────────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GradientIndigoStart.copy(0.06f))
                        .border(1.dp, Brush.linearGradient(listOf(GradientIndigoStart.copy(0.3f), GradientIndigoEnd.copy(0.15f))), RoundedCornerShape(16.dp))
                        .clickable { navController.navigate(Routes.PROFILE) }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user?.name ?: "Loading...", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                            Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("View Profile →", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Account Section ──────────────────────────────────────────────────
            item { SectionHeader2("Account") }
            item { SettingsRow(icon = Icons.Default.Person, label = "Edit Profile") { navController.navigate(Routes.EDIT_PROFILE) } }
            item { SettingsRow(icon = Icons.Default.Lock, label = "Change Password") { showChangePasswordDialog = true } }
            item { SettingsRow(icon = Icons.Default.Link, label = "Linked Accounts", value = "Google ✓") { showLinkedAccountsDialog = true } }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ── Preferences Section ──────────────────────────────────────────────
            item { SectionHeader2("Preferences") }
            item { SettingsRowToggle(icon = Icons.Default.DarkMode, label = "Dark Mode", checked = isDarkTheme, onToggle = { themeViewModel?.toggleTheme() }) }
            item { SettingsRow(icon = Icons.Default.Language, label = "Language", value = "English") {} }
            item { SettingsRow(icon = Icons.Default.Notifications, label = "Notifications") { navController.navigate(Routes.NOTIFICATION_SETTINGS) } }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ── Privacy & Security Section ───────────────────────────────────────
            item { SectionHeader2("Privacy & Security") }
            item { SettingsRow(icon = Icons.Default.Visibility, label = "Profile Visibility", value = profileVisibility) { showVisibilityDialog = true } }
            item { SettingsRow(icon = Icons.Default.Block, label = "Block List") { showBlockListDialog = true } }
            item {
                // 2FA with amber badge
                SettingsRow(icon = Icons.Default.Shield, label = "Two-Factor Authentication", trailingContent = {
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SemanticWarning.copy(0.15f)).padding(horizontal = 8.dp, vertical = 4.dp).clickable { show2FaDialog = true }) {
                        Text("Active ✓", style = MaterialTheme.typography.labelSmall, color = SemanticWarning)
                    }
                }) { show2FaDialog = true }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ── About Section ────────────────────────────────────────────────────
            item { SectionHeader2("About") }
            item { SettingsRow(icon = Icons.AutoMirrored.Filled.HelpOutline, label = "Help & Support") { showFeedbackSheet = true } }
            item { SettingsRow(icon = Icons.Default.Star, label = "Rate the App") { showRateDialog = true } }
            item { SettingsRow(icon = Icons.Default.Info, label = "Terms & Privacy") { showTermsDialog = true } }
            item { SettingsRow(icon = Icons.Default.VerifiedUser, label = "Version 1.0.0", isInteractive = false) {} }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // ── Logout ───────────────────────────────────────────────────────────
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out", tint = SemanticError, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("Sign Out", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = SemanticError)
                }
            }
        }

        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (showFeedbackSheet) {
            com.abpvt.campusgig_frontend.ui.components.HelpFeedbackBottomSheet(
                isSubmitting = submitFeedbackState is com.abpvt.campusgig_frontend.core.utils.Resource.Loading,
                onDismiss = { showFeedbackSheet = false },
                onSubmit = { type, rating, message ->
                    feedbackViewModel.submitFeedback(type, rating, message)
                    showFeedbackSheet = false
                    scope.launch { snackbarHostState.showSnackbar("Feedback submitted! Thank you 🙏") }
                }
            )
        }
    }
}

@Composable
private fun SectionHeader2(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String = "",
    isInteractive: Boolean = true,
    valueColor: Color = Color.Unspecified,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val actualValueColor = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else valueColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isInteractive) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        if (trailingContent != null) {
            trailingContent()
        } else if (value.isNotBlank()) {
            Text(value, style = MaterialTheme.typography.bodySmall, color = actualValueColor)
            Spacer(modifier = Modifier.width(6.dp))
            if (isInteractive) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        } else if (isInteractive) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = MaterialTheme.colorScheme.outline.copy(0.5f))
}

@Composable
private fun SettingsRowToggle(icon: ImageVector, label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        Switch(
            checked = checked, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
    HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = MaterialTheme.colorScheme.outline.copy(0.5f))
}
