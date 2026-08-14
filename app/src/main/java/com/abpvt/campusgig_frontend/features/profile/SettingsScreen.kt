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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
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
import com.abpvt.campusgig_frontend.ui.theme.BorderSubtle
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.Indigo400
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import com.abpvt.campusgig_frontend.ui.theme.Surface1
import com.abpvt.campusgig_frontend.ui.theme.Surface2
import com.abpvt.campusgig_frontend.ui.theme.Surface3
import com.abpvt.campusgig_frontend.ui.theme.TextPrimary
import com.abpvt.campusgig_frontend.ui.theme.TextSecondary
import com.abpvt.campusgig_frontend.ui.theme.TextTertiary
import com.abpvt.campusgig_frontend.CampusGigApplication

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).userRepository
        )
    )
) {
    val profileState by viewModel.profile.collectAsState()
    val user = (profileState as? Resource.Success<User>)?.data

    var darkMode by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary) },
            text = { Text("You'll need to sign in again to access your account.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary) },
            confirmButton = {
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(SemanticError).clickable { showLogoutDialog = false; navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Sign Out", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            },
            dismissButton = {
                Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Surface3).clickable { showLogoutDialog = false }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                }
            },
            containerColor = Surface2,
            shape = RoundedCornerShape(20.dp)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Surface1),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
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
                        Text(user?.name ?: "Loading...", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                        Text(user?.email ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("View Profile →", style = MaterialTheme.typography.labelSmall, color = Indigo400)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Account Section ──────────────────────────────────────────────────
        item { SectionHeader2("Account") }
        item { SettingsRow(icon = Icons.Default.Person, label = "Edit Profile") { navController.navigate(Routes.EDIT_PROFILE) } }
        item { SettingsRow(icon = Icons.Default.Lock, label = "Change Password") {} }
        item { SettingsRow(icon = Icons.Default.Link, label = "Linked Accounts", value = "Google ✓") {} }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // ── Preferences Section ──────────────────────────────────────────────
        item { SectionHeader2("Preferences") }
        item { SettingsRowToggle(icon = Icons.Default.DarkMode, label = "Dark Mode", checked = darkMode, onToggle = { darkMode = it }) }
        item { SettingsRow(icon = Icons.Default.Language, label = "Language", value = "English") {} }
        item { SettingsRow(icon = Icons.Default.Notifications, label = "Notifications") { navController.navigate(Routes.NOTIFICATION_SETTINGS) } }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // ── Privacy & Security Section ───────────────────────────────────────
        item { SectionHeader2("Privacy & Security") }
        item { SettingsRow(icon = Icons.Default.Visibility, label = "Profile Visibility", value = "Public") {} }
        item { SettingsRow(icon = Icons.Default.Block, label = "Block List") {} }
        item {
            // 2FA with amber badge
            SettingsRow(icon = Icons.Default.Shield, label = "Two-Factor Authentication", trailingContent = {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SemanticWarning.copy(0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Enable", style = MaterialTheme.typography.labelSmall, color = SemanticWarning)
                }
            })
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // ── About Section ────────────────────────────────────────────────────
        item { SectionHeader2("About") }
        item { SettingsRow(icon = Icons.Default.HelpOutline, label = "Help & Support") {} }
        item { SettingsRow(icon = Icons.Default.Star, label = "Rate the App") {} }
        item { SettingsRow(icon = Icons.Default.Info, label = "Terms & Privacy") {} }
        item { SettingsRow(icon = Icons.Default.VerifiedUser, label = "Version 1.0.0", isInteractive = false, valueColor = TextTertiary) {} }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // ── Logout ───────────────────────────────────────────────────────────
        item {
            HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 20.dp))
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
}

@Composable
private fun SectionHeader2(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
        color = TextTertiary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String = "",
    isInteractive: Boolean = true,
    valueColor: Color = TextTertiary,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isInteractive) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
        if (trailingContent != null) {
            trailingContent()
        } else if (value.isNotBlank()) {
            Text(value, style = MaterialTheme.typography.bodySmall, color = valueColor)
            Spacer(modifier = Modifier.width(6.dp))
            if (isInteractive) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
        } else if (isInteractive) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = BorderSubtle.copy(0.5f))
}

@Composable
private fun SettingsRowToggle(icon: ImageVector, label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
        Switch(
            checked = checked, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Indigo400, uncheckedTrackColor = Surface3)
        )
    }
    HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = BorderSubtle.copy(0.5f))
}
