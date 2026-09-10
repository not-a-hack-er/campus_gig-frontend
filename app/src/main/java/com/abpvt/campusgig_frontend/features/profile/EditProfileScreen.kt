/**
 * EditProfileScreen.kt — v2.1 — Edit Profile
 *
 * CONCEPT: Editing your profile should feel empowering, not tedious.
 * Sections are collapsible for focus. Auto-save bar at bottom.
 *
 * FEATURES:
 * - Gradient palette picker for banner (8 preset options as circles)
 * - Avatar circle showing current photo or initials with photo picker
 * - Collapsible form sections (Basic Info, Campus, Skills, Portfolio)
 * - Skills chip editor with add/remove
 * - Auto-save indicator bar (fades after 2s)
 * - Gradient save button at top-right
 */
package com.abpvt.campusgig_frontend.features.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ProfileViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import kotlinx.coroutines.delay

private val bannerGradients = listOf(
    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),     // Indigo-Violet
    listOf(Color(0xFF14B8A6), Color(0xFF06B6D4)),     // Teal-Cyan
    listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),     // Amber-Red
    listOf(Color(0xFF10B981), Color(0xFF3B82F6)),     // Green-Blue
    listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),     // Pink-Violet
    listOf(Color(0xFF6366F1), Color(0xFF14B8A6)),     // Indigo-Teal
    listOf(Color(0xFFF97316), Color(0xFFEF4444)),     // Orange-Red
    listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)),     // Violet-Blue
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).userRepository
        )
    )
) {
    val context = LocalContext.current
    val profileState by viewModel.profile.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val avatarUploadState by viewModel.avatarUploadState.collectAsState()
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.uploadAvatar(context.contentResolver, uri)
    }

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    var skillInput by remember { mutableStateOf("") }
    val skills = remember { mutableStateListOf<String>() }
    var github by remember { mutableStateOf("") }
    var linkedin by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var resumeUrl by remember { mutableStateOf("") }
    var selectedBannerGradient by remember { mutableStateOf(0) }

    var showAutoSave by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf("") }

    // Section expanded states
    var basicExpanded by remember { mutableStateOf(true) }
    var campusExpanded by remember { mutableStateOf(false) }
    var skillsExpanded by remember { mutableStateOf(false) }
    var linksExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(profileState) {
        if (profileState is Resource.Success) {
            val user = (profileState as Resource.Success<User>).data
            name = user.name
            bio = user.bio
            college = user.college
            course = user.branch
            selectedYear = user.yearOfStudy
            skills.clear()
            skills.addAll(user.skills)
            github = user.githubProfile
            linkedin = user.linkedinProfile
            website = user.portfolioLinks.firstOrNull() ?: ""
            resumeUrl = user.resumeUrl
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            showAutoSave = true
            delay(2000)
            showAutoSave = false
            viewModel.resetUpdateState()
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Edit Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )

                // Save button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                        .clickable(enabled = updateState !is Resource.Loading) {
                            localError = when {
                                name.isBlank() -> "Full Name is required"
                                college.isBlank() -> "College is required"
                                else -> ""
                            }
                            if (localError.isEmpty()) {
                                val current = (profileState as? Resource.Success<User>)?.data ?: User()
                                viewModel.updateProfile(
                                    current.copy(
                                        name = name.trim(),
                                        bio = bio.trim(),
                                        college = college.trim(),
                                        branch = course.trim(),
                                        yearOfStudy = selectedYear,
                                        skills = skills.toList(),
                                        githubProfile = github.trim(),
                                        linkedinProfile = linkedin.trim(),
                                        resumeUrl = resumeUrl.trim(),
                                        portfolioLinks = if (website.isNotBlank()) listOf(website.trim()) else current.portfolioLinks
                                    )
                                )
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (updateState is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Banner & Avatar ───────────────────────────────────────────
                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    // Banner gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Brush.linearGradient(bannerGradients[selectedBannerGradient]))
                    )
                    // Gradient palette picker
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bannerGradients.take(4).forEachIndexed { index, colors ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(colors))
                                    .then(if (selectedBannerGradient == index) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                                    .clickable { selectedBannerGradient = index },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedBannerGradient == index) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    // Avatar — tap to choose and upload a replacement photo.
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = 20.dp, y = 36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                                .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .clickable(enabled = avatarUploadState !is Resource.Loading) {
                                    avatarPicker.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val remoteAvatar = (profileState as? Resource.Success<User>)?.data?.profilePicture
                            if (!remoteAvatar.isNullOrBlank()) {
                                AsyncImage(
                                    model = remoteAvatar,
                                    contentDescription = "Profile photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            if (avatarUploadState is Resource.Loading) {
                                Box(
                                    Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                }
                            } else {
                                Box(
                                    Modifier.align(Alignment.BottomEnd).size(24.dp)
                                        .clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PhotoCamera, "Change profile photo", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
                Spacer(modifier = Modifier.height(16.dp))

                // Error row
                if (localError.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SemanticError.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text("⚠ $localError", color = SemanticError, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                (avatarUploadState as? Resource.Error)?.message?.let { message ->
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(8.dp)).background(SemanticError.copy(alpha = 0.1f)).padding(12.dp)
                    ) {
                        Text(message, color = SemanticError, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ── Collapsible Sections ───────────────────────────────────────
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                // Basic Info section
                SectionHeader("Basic Info", basicExpanded) { basicExpanded = !basicExpanded }
                AnimatedVisibility(visible = basicExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionLabel("Full Name *")
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; localError = "" },
                            placeholder = { Text("Enter your full name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionLabel("Bio (${bio.length}/160)")
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { if (it.length <= 160) bio = it },
                            placeholder = { Text("Tell us about yourself...") },
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Campus section
                SectionHeader("Campus", campusExpanded) { campusExpanded = !campusExpanded }
                AnimatedVisibility(visible = campusExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionLabel("College *")
                        OutlinedTextField(
                            value = college,
                            onValueChange = { college = it; localError = "" },
                            placeholder = { Text("e.g. GL Bajaj Institute") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionLabel("Course / Branch")
                        OutlinedTextField(
                            value = course,
                            onValueChange = { course = it },
                            placeholder = { Text("e.g. Computer Science") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionLabel("Year of Study")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1st", "2nd", "3rd", "4th").forEach { year ->
                                val isSelected = selectedYear == year
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                                            else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                        )
                                        .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                        .clickable { selectedYear = year }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        year,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Skills section
                SectionHeader("Skills", skillsExpanded) { skillsExpanded = !skillsExpanded }
                AnimatedVisibility(visible = skillsExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            skills.forEach { skill ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GradientIndigoStart.copy(alpha = 0.12f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(skill, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp).clickable { skills.remove(skill) }
                                        )
                                    }
                                }
                            }
                        }
                        if (skills.isNotEmpty()) Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = skillInput,
                            onValueChange = { skillInput = it },
                            placeholder = { Text("Add skill... (press Enter)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                val candidate = skillInput.trim().take(50)
                                if (candidate.isNotEmpty() && skills.none { it.equals(candidate, ignoreCase = true) }) {
                                    skills.add(candidate)
                                }
                                skillInput = ""
                            }),
                            trailingIcon = {
                                if (skillInput.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .clickable {
                                                val candidate = skillInput.trim().take(50)
                                                if (candidate.isNotEmpty() && skills.none { it.equals(candidate, ignoreCase = true) }) {
                                                    skills.add(candidate)
                                                }
                                                skillInput = ""
                                            }
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Portfolio Links
                SectionHeader("Portfolio Links", linksExpanded) { linksExpanded = !linksExpanded }
                AnimatedVisibility(visible = linksExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionLabel("🐙 GitHub URL")
                        OutlinedTextField(
                            value = github,
                            onValueChange = { github = it },
                            placeholder = { Text("github.com/username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionLabel("💼 LinkedIn URL")
                        OutlinedTextField(
                            value = linkedin,
                            onValueChange = { linkedin = it },
                            placeholder = { Text("linkedin.com/in/username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionLabel("🔗 Portfolio / Website")
                        OutlinedTextField(
                            value = website,
                            onValueChange = { website = it },
                            placeholder = { Text("yourwebsite.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionLabel("📄 Resume / CV Link (Google Drive, Notion, PDF)")
                        OutlinedTextField(
                            value = resumeUrl,
                            onValueChange = { resumeUrl = it },
                            placeholder = { Text("https://drive.google.com/... or resume link") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // ── Auto-save indicator (bottom) ──────────────────────────────────────
        AnimatedVisibility(
            visible = showAutoSave,
            enter = expandVertically(expandFrom = Alignment.Bottom),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SemanticSuccess.copy(alpha = 0.9f))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Changes saved ✓", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, isExpanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}
