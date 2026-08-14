/**
 * NotificationSettingsScreen.kt — Fine-grained push/email controls
 *
 * CONCEPT: Clean grouped toggles. Master switch controls all below.
 * Quiet hours time pickers appear when DND is on.
 *
 * SECTIONS:
 * - Master "Push Notifications" toggle (prominent card)
 * - Notification types (only active when master is ON)
 * - Quiet hours (DND toggle + time pickers when on)
 * - Email notifications
 */
package com.abpvt.campusgig_frontend.features.notifications

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.ui.theme.BorderSubtle
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.Indigo400
import com.abpvt.campusgig_frontend.ui.theme.Surface1
import com.abpvt.campusgig_frontend.ui.theme.Surface2
import com.abpvt.campusgig_frontend.ui.theme.Surface3
import com.abpvt.campusgig_frontend.ui.theme.TextPrimary
import com.abpvt.campusgig_frontend.ui.theme.TextSecondary
import com.abpvt.campusgig_frontend.ui.theme.TextTertiary

@Composable
fun NotificationSettingsScreen(navController: NavController) {
    var pushEnabled by remember { mutableStateOf(true) }
    var applicationsEnabled by remember { mutableStateOf(true) }
    var messagesEnabled by remember { mutableStateOf(true) }
    var messagePreviewEnabled by remember { mutableStateOf(false) }
    var communityEnabled by remember { mutableStateOf(true) }
    var gigsEnabled by remember { mutableStateOf(true) }
    var dndEnabled by remember { mutableStateOf(false) }
    var emailWeeklyEnabled by remember { mutableStateOf(true) }
    var emailMatchEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Surface1),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Surface2).border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)).clickable { navController.popBackStack() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Notifications", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
            }
        }

        // ── Master Toggle Card ────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (pushEnabled)
                            Brush.linearGradient(listOf(GradientIndigoStart.copy(0.1f), GradientIndigoEnd.copy(0.05f)))
                        else
                            Brush.linearGradient(listOf(Surface2, Surface2))
                    )
                    .border(
                        1.5.dp,
                        if (pushEnabled)
                            Brush.linearGradient(listOf(GradientIndigoStart.copy(0.5f), GradientIndigoEnd.copy(0.3f)))
                        else
                            Brush.linearGradient(listOf(BorderSubtle, BorderSubtle)),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (pushEnabled) GradientIndigoStart.copy(0.15f) else Surface3),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (pushEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications, contentDescription = null, tint = if (pushEnabled) Indigo400 else TextTertiary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Push Notifications", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                        Text(if (pushEnabled) "Notifications are active" else "All notifications paused", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(checked = pushEnabled, onCheckedChange = { pushEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Indigo400, uncheckedTrackColor = Surface3))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Notification Types ────────────────────────────────────────────────
        item { SectionLabel("Notification Types") }
        item {
            NotifToggleRow(
                icon = Icons.Default.Work, label = "Applications",
                desc = "New applications & status changes",
                enabled = pushEnabled, checked = applicationsEnabled, onToggle = { if (pushEnabled) applicationsEnabled = it }
            )
        }
        item {
            Column {
                NotifToggleRow(
                    icon = Icons.Default.Chat, label = "Messages",
                    desc = "New chat messages",
                    enabled = pushEnabled, checked = messagesEnabled, onToggle = { if (pushEnabled) messagesEnabled = it }
                )
                if (messagesEnabled && pushEnabled) {
                    NotifToggleRow(
                        icon = null, label = "Preview in notification",
                        desc = "Show message content in notification",
                        enabled = true, checked = messagePreviewEnabled, onToggle = { messagePreviewEnabled = it },
                        indented = true
                    )
                }
            }
        }
        item {
            NotifToggleRow(
                icon = Icons.Default.Groups, label = "Community",
                desc = "New posts in your communities",
                enabled = pushEnabled, checked = communityEnabled, onToggle = { if (pushEnabled) communityEnabled = it }
            )
        }
        item {
            NotifToggleRow(
                icon = Icons.Default.BusinessCenter, label = "Gig Matches",
                desc = "Gigs matching your skills",
                enabled = pushEnabled, checked = gigsEnabled, onToggle = { if (pushEnabled) gigsEnabled = it }
            )
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }

        // ── Quiet Hours ───────────────────────────────────────────────────────
        item { SectionLabel("Quiet Hours") }
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(14.dp)).background(Surface2).border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bedtime, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Do Not Disturb", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                        Text("Mute all notifications during quiet hours", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                    Switch(checked = dndEnabled, onCheckedChange = { dndEnabled = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Indigo400, uncheckedTrackColor = Surface3))
                }

                if (dndEnabled) {
                    HorizontalDivider(color = BorderSubtle)
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("From", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(GradientIndigoStart.copy(0.1f)).border(1.dp, Indigo400.copy(0.3f), RoundedCornerShape(10.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text("11:00 PM", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Indigo400)
                            }
                        }
                        Text("→", style = MaterialTheme.typography.titleMedium, color = TextTertiary)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Until", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(GradientIndigoStart.copy(0.1f)).border(1.dp, Indigo400.copy(0.3f), RoundedCornerShape(10.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text("7:00 AM", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Indigo400)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Email Notifications ───────────────────────────────────────────────
        item { SectionLabel("Email Notifications") }
        item {
            NotifToggleRow(icon = null, label = "Weekly Digest", desc = "A summary of activity every week", enabled = true, checked = emailWeeklyEnabled, onToggle = { emailWeeklyEnabled = it })
        }
        item {
            NotifToggleRow(icon = null, label = "Gig Match Emails", desc = "Emails when new matching gigs are posted", enabled = true, checked = emailMatchEnabled, onToggle = { emailMatchEnabled = it })
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = TextTertiary, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
}

@Composable
private fun NotifToggleRow(
    icon: ImageVector?,
    label: String,
    desc: String,
    enabled: Boolean,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    indented: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indented) 52.dp else 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null && !indented) {
            Icon(icon, contentDescription = null, tint = if (enabled) TextSecondary else TextTertiary.copy(0.5f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = if (enabled) TextPrimary else TextTertiary)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = if (enabled) TextSecondary else TextTertiary.copy(0.5f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Indigo400, uncheckedTrackColor = Surface3, disabledUncheckedTrackColor = Surface3.copy(0.5f))
        )
    }
    HorizontalDivider(modifier = Modifier.padding(start = if (indented) 52.dp else 54.dp), color = BorderSubtle.copy(0.5f))
}
