/**
 * CreateCommunityScreen.kt — v2.0 — Create a Community
 *
 * CONCEPT: Creating a community is significant — the screen feels important, creative.
 *
 * FEATURES:
 * - Banner gradient picker (8 preset circles as color options)
 * - Community avatar with camera overlay
 * - Form: Name, Description, Category, Privacy cards, College restriction, Tags
 * - CREATE button gradient full-width
 */
package com.abpvt.campusgig_frontend.features.communities

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.CommunityViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Community
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError

private val bannerOptions = listOf(
    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
    listOf(Color(0xFF14B8A6), Color(0xFF06B6D4)),
    listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
    listOf(Color(0xFF10B981), Color(0xFF3B82F6)),
    listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
    listOf(Color(0xFF6366F1), Color(0xFF14B8A6)),
    listOf(Color(0xFFF97316), Color(0xFFEF4444)),
    listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)),
)

private val communityCategories2 = listOf("💻 Coding", "🎨 Design", "🎓 Campus Life", "📚 Study Groups", "🚀 Entrepreneurship", "🎵 Music", "📷 Photography", "🤝 Networking")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateCommunityScreen(
    navController: NavController,
    viewModel: CommunityViewModel = viewModel(
        factory = CommunityViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).communityRepository
        )
    )
) {
    val createState by viewModel.createState.collectAsState()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var openToAll by remember { mutableStateOf(true) }
    var selectedBanner by remember { mutableStateOf(0) }
    var tagInput by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>() }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(createState) {
        if (createState is Resource.Success) {
            viewModel.resetCreateState()
            navController.popBackStack()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onBackground, unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary, focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { navController.popBackStack() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Create Community", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
            }

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                // Banner area
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Brush.linearGradient(bannerOptions[selectedBanner])),
                        contentAlignment = Alignment.Center
                    ) {
                        if (name.isEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White.copy(0.7f), modifier = Modifier.size(28.dp))
                                Text("Community Banner", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
                            }
                        }
                        // Banner palette row
                        Row(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            bannerOptions.forEachIndexed { i, colors ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(colors))
                                        .then(if (selectedBanner == i) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                                        .clickable { selectedBanner = i },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedBanner == i) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                    // Avatar
                    Box(modifier = Modifier.align(Alignment.BottomStart).offset(x = 20.dp, y = 28.dp).size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background).padding(3.dp)) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Brush.linearGradient(bannerOptions[selectedBanner])), contentAlignment = Alignment.Center) {
                            Text(name.firstOrNull()?.uppercaseChar()?.toString() ?: "?", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                        Box(modifier = Modifier.align(Alignment.BottomEnd).size(18.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    if (error.isNotBlank()) {
                        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(SemanticError.copy(0.1f)).padding(12.dp)) {
                            Text("⚠ $error", color = SemanticError, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    FormLabel("Community Name *")
                    OutlinedTextField(value = name, onValueChange = { if (it.length <= 60) name = it; error = "" },
                        placeholder = { Text("e.g. Android Devs @ IIT Delhi") }, singleLine = true,
                        trailingIcon = { Text("${name.length}/60", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors)
                    Spacer(modifier = Modifier.height(14.dp))

                    FormLabel("Description")
                    OutlinedTextField(value = description, onValueChange = { description = it },
                        placeholder = { Text("What is this community about?") }, minLines = 4, maxLines = 6,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors)
                    Spacer(modifier = Modifier.height(14.dp))

                    FormLabel("Category")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        communityCategories2.forEach { cat ->
                            val isSel = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)) else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)))
                                    .border(1.dp, if (isSel) Color.Transparent else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(cat, style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal), color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    FormLabel("Privacy")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Public card
                        PrivacyCard(
                            selected = !isPrivate,
                            icon = { Icon(Icons.Default.Public, contentDescription = null, tint = if (!isPrivate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) },
                            label = "Public",
                            desc = "Anyone can find and join",
                            modifier = Modifier.weight(1f),
                            onClick = { isPrivate = false }
                        )
                        // Private card
                        PrivacyCard(
                            selected = isPrivate,
                            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = if (isPrivate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) },
                            label = "Private",
                            desc = "Invite only",
                            modifier = Modifier.weight(1f),
                            onClick = { isPrivate = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Open to all toggle
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)).padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Open to all colleges", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onBackground)
                                Text("Allow students from any college to join", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                            Switch(
                                checked = openToAll, onCheckedChange = { openToAll = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    FormLabel("Tags (press Enter)")
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        placeholder = { Text("e.g. kotlin, android, compose") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        trailingIcon = {
                            if (tagInput.isNotBlank()) {
                                Box(modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary).clickable { if (tagInput.isNotBlank()) { tags.add(tagInput.trim()); tagInput = "" } }.padding(4.dp), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    )
                    if (tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            tags.forEach { tag ->
                                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(0.1f)).border(1.dp, MaterialTheme.colorScheme.primary.copy(0.3f), RoundedCornerShape(8.dp)).padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(tag, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp).clickable { tags.remove(tag) })
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // CREATE button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                            .clickable(enabled = createState !is Resource.Loading) {
                                error = when {
                                    name.isBlank() -> "Community name is required"
                                    description.isBlank() -> "Please add a description"
                                    selectedCategory.isBlank() -> "Please select a category"
                                    else -> ""
                                }
                                if (error.isEmpty()) {
                                    viewModel.createCommunity(
                                        Community(
                                            name = name.trim(),
                                            description = description.trim(),
                                            category = selectedCategory.replace(Regex("^[^ ]+ "), "").trim(),
                                            isPrivate = isPrivate
                                        )
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (createState is Resource.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Create Community 🌟", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun PrivacyCard(selected: Boolean, icon: @Composable () -> Unit, label: String, desc: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) GradientIndigoStart.copy(0.06f) else MaterialTheme.colorScheme.surface)
            .border(
                1.5.dp,
                if (selected) Brush.linearGradient(listOf(GradientIndigoStart.copy(0.6f), GradientIndigoEnd.copy(0.3f)))
                else Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline)),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                icon()
                if (selected) Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}
