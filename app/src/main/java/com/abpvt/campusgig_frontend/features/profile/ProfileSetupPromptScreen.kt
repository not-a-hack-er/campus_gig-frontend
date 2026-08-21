/**
 * ProfileSetupPromptScreen.kt — First-time post-login profile completion
 *
 * CONCEPT: Feels like building your identity, not homework.
 * Accordion-style checklist with inline forms when tapped.
 * Progress bar shows 70% complete after basic fields.
 */
package com.abpvt.campusgig_frontend.features.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupPromptScreen(navController: NavController) {
    val basicDone = true
    var skillsDone by remember { mutableStateOf(false) }
    var photoDone by remember { mutableStateOf(false) }
    var linksDone by remember { mutableStateOf(false) }

    var expandedItem by remember { mutableStateOf<Int?>(null) }
    var skillInput by remember { mutableStateOf("") }
    val skills = remember { mutableStateListOf<String>() }
    var github by remember { mutableStateOf("") }
    var linkedin by remember { mutableStateOf("") }

    val completedCount = listOf(basicDone, skillsDone, photoDone, linksDone).count { it }
    val progress = completedCount / 4f

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary, focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(GradientIndigoStart.copy(0.12f), Color(0xFF0A0C12))))
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(48.dp))

            // Progress bar
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(progress * 100).toInt()}% Complete", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                Text("$completedCount of 4 steps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text("Let's make your profile shine ✨", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text("A complete profile gets 3× more gig responses", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(28.dp))

            // ── Checklist ─────────────────────────────────────────────────────

            // 1. Basic Info — already done
            ChecklistItem(
                index = 0, label = "Basic info", timeEst = "Done ✓", isDone = basicDone,
                isExpanded = expandedItem == 0, onToggle = { expandedItem = if (expandedItem == 0) null else 0 }
            ) { /* no form — already done */ }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Add skills
            ChecklistItem(
                index = 1, label = "Add your skills", timeEst = "15 sec", isDone = skillsDone,
                isExpanded = expandedItem == 1, onToggle = { expandedItem = if (expandedItem == 1) null else 1 }
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        skills.forEach { skill ->
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(0.1f)).border(1.dp, MaterialTheme.colorScheme.primary.copy(0.3f), RoundedCornerShape(8.dp)).padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(skill, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(modifier = Modifier.size(14.dp).clickable { skills.remove(skill) }, contentAlignment = Alignment.Center) {
                                        androidx.compose.material3.Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (skills.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = skillInput, onValueChange = { skillInput = it },
                        placeholder = { Text("Type a skill and press add") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors,
                        trailingIcon = {
                            if (skillInput.isNotBlank()) {
                                Box(modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary).clickable { skills.add(skillInput.trim()); skillInput = "" }.padding(4.dp)) {
                                    androidx.compose.material3.Icon(Icons.Default.Check, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        })
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))).clickable(enabled = skills.isNotEmpty()) { skillsDone = true; expandedItem = null }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Save Skills", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Add photo
            ChecklistItem(
                index = 2, label = "Add a profile photo", timeEst = "10 sec", isDone = photoDone,
                isExpanded = expandedItem == 2, onToggle = { expandedItem = if (expandedItem == 2) null else 2 }
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📷", fontSize = 24.sp)
                            Text("Tap to choose a photo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))).clickable { photoDone = true; expandedItem = null }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Upload Photo", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Portfolio links
            ChecklistItem(
                index = 3, label = "Add portfolio links", timeEst = "1 min", isDone = linksDone,
                isExpanded = expandedItem == 3, onToggle = { expandedItem = if (expandedItem == 3) null else 3 }
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(value = github, onValueChange = { github = it }, placeholder = { Text("🐙 GitHub URL") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = linkedin, onValueChange = { linkedin = it }, placeholder = { Text("💼 LinkedIn URL") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))).clickable(enabled = github.isNotBlank() || linkedin.isNotBlank()) { linksDone = true; expandedItem = null }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Save Links", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // CTA buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                    .clickable { navController.navigate(Routes.HOME) { popUpTo(Routes.PROFILE_SETUP) { inclusive = true } } },
                contentAlignment = Alignment.Center
            ) {
                Text("Complete Profile →", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "I'll do this later",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { navController.navigate(Routes.HOME) { popUpTo(Routes.PROFILE_SETUP) { inclusive = true } } }
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ChecklistItem(
    index: Int,
    label: String,
    timeEst: String,
    isDone: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDone) SemanticSuccess.copy(0.06f) else MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (isDone) SemanticSuccess.copy(0.3f) else if (isExpanded) MaterialTheme.colorScheme.primary.copy(0.4f) else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status circle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isDone) SemanticSuccess else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (isDone) SemanticSuccess else MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        androidx.compose.material3.Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text("${index + 1}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = if (isDone) SemanticSuccess else MaterialTheme.colorScheme.onBackground)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (isDone) SemanticSuccess.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(timeEst, style = MaterialTheme.typography.labelSmall, color = if (isDone) SemanticSuccess else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }

            if (!isDone) {
                AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    content()
                }
            }
        }
    }
}
