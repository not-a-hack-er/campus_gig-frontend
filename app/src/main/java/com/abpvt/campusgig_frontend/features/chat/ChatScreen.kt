/**
 * ChatScreen.kt — v3.1 — Premium Real-time Private Chat (Applicant ↔ Gig Poster)
 *
 * CONCEPT: Modern, iMessage / Telegram level messaging experience tailored
 * for campus freelancers and gig employers. Fluid, alive, trustworthy.
 *
 * FEATURES:
 * - Header: Ambient indigo glow + Avatar + Name + Real-time Online/Offline indicator
 * - Context Menu (View Profile, View Gig)
 * - Collapsible Glassmorphic Gig Context Banner (Gig Title + Budget)
 * - Smart Date Separators ("Today", "Yesterday", "MMM d, yyyy")
 * - Sender bubbles: Indigo-violet gradient with asymmetric radius
 * - Receiver bubbles: MaterialTheme.colorScheme.surfaceVariant dark card with subtle border
 * - Real-time Delivery Receipts (✓ / ✓✓) + dim "sending" state on temp messages
 * - Animated Typing Indicator with peer avatar (3 bouncing dots)
 * - Staggered message entry animations (animateItem)
 * - Premium Empty State with gradient ring
 * - Gradient scrim separator above input bar
 * - Premium Input Bar with spring press animation
 */
package com.abpvt.campusgig_frontend.features.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.ChatViewModelFactory
import com.abpvt.campusgig_frontend.core.utils.Constants
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Message
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.components.AvatarInitials
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.GradientTealEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientTealStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.Violet400
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChatScreen(
    navController: NavController,
    receiverId: String,
    receiverName: String,
    gigTitle: String? = null,
    gigBudget: String? = null,
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(
            (LocalContext.current.applicationContext as CampusGigApplication).chatRepository
        )
    )
) {
    val context = LocalContext.current
    val currentUserId = remember {
        context.getSharedPreferences(Constants.PREFS_NAME, android.content.Context.MODE_PRIVATE)
            .getString(Constants.KEY_USER_ID, "") ?: ""
    }

    LaunchedEffect(receiverId) {
        viewModel.loadMessages(receiverId, currentUserId)
    }

    val messagesState by viewModel.messages.collectAsState()
    val isPeerTyping by viewModel.isPeerTyping.collectAsState()
    val isPeerOnline by viewModel.isPeerOnline.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showGigBanner by remember { mutableStateOf(!gigTitle.isNullOrBlank()) }
    var showMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Smooth auto-scroll on new message
    LaunchedEffect(messagesState) {
        val msgList = (messagesState as? Resource.Success<List<Message>>)?.data
        if (!msgList.isNullOrEmpty()) {
            listState.animateScrollToItem(msgList.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header with ambient indigo glow ───────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Ambient glow layer behind the header content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            radius = 500f
                        )
                    )
            )
            // Bottom border line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                Violet400.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Arrow Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .clickable {
                            viewModel.cleanup()
                            navController.popBackStack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Receiver Avatar (Clickable to view public profile)
                Box(
                    modifier = Modifier.clickable {
                        navController.navigate(Routes.publicProfile(receiverId))
                    }
                ) {
                    // Avatar with subtle online ring
                    Box {
                        AvatarInitials(name = receiverName, size = 38)
                        // Online indicator dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPeerOnline || isPeerTyping) SemanticSuccess
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Receiver Name + Real-time Status
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            navController.navigate(Routes.publicProfile(receiverId))
                        }
                ) {
                    Text(
                        text = receiverName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = when {
                            isPeerTyping -> "typing..."
                            isPeerOnline -> "Active now"
                            else -> "Offline"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = when {
                            isPeerTyping -> MaterialTheme.colorScheme.primary
                            isPeerOnline -> SemanticSuccess
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }

                // ── Context Dropdown Menu (MoreVert) ──────────────────────────
                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Profile", color = MaterialTheme.colorScheme.onBackground) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showMenu = false
                                navController.navigate(Routes.publicProfile(receiverId))
                            }
                        )
                        if (!gigTitle.isNullOrBlank()) {
                            DropdownMenuItem(
                                text = { Text("View Gig", color = MaterialTheme.colorScheme.onBackground) },
                                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    showMenu = false
                                    showGigBanner = true
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Gig Context Banner (Applicant ↔ Gig Poster Context) ─────────────
        AnimatedVisibility(
            visible = showGigBanner && !gigTitle.isNullOrBlank(),
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -20 },
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A1A1A))
                    .border(
                        width = 0.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(GradientTealStart.copy(alpha = 0.5f), GradientTealEnd.copy(alpha = 0.2f))
                        ),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GradientTealStart.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📋", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GIG DISCUSSION",
                                style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GradientTealEnd, letterSpacing = 0.8.sp)
                            )
                            Text(
                                text = gigTitle ?: "Gig Details",
                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground),
                                maxLines = 1
                            )
                        }
                    }

                    if (!gigBudget.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GradientTealStart.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = gigBudget,
                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GradientTealEnd)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = "✕",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { showGigBanner = false }
                            .padding(4.dp)
                    )
                }
            }
        }

        // ── Message List ──────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            // Subtle ambient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.025f), Color.Transparent),
                            radius = 800f
                        )
                    )
            )

            when (val state = messagesState) {
                is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                }
                is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                is Resource.Success<List<Message>> -> {
                    val messages = state.data
                    if (messages.isEmpty()) {
                        // ── Premium Empty State ───────────────────────────────
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                // Gradient ring container
                                Box(
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(GradientIndigoStart.copy(alpha = 0.4f), Violet400.copy(alpha = 0.3f))
                                            )
                                        )
                                        .padding(3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.background),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("💬", fontSize = 36.sp)
                                    }
                                }
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    text = "Start chatting with",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = receiverName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Discuss gig scope, deliverables, and timelines in real-time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    } else {
                        val groupedMessages = remember(messages) { groupMessagesByDate(messages) }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            groupedMessages.forEach { (dateLabel, messageList) ->
                                item(key = "date_$dateLabel") {
                                    DateSeparator(label = dateLabel)
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                items(messageList, key = { it.id }) { message ->
                                    val isMyMessage = message.sender?.id == currentUserId
                                    val isSending = message.id.startsWith("temp_")
                                    MessageBubble(
                                        message = message,
                                        isMyMessage = isMyMessage,
                                        isSending = isSending
                                    )
                                    Spacer(modifier = Modifier.height(if (isMyMessage) 4.dp else 10.dp))
                                }
                            }

                            // Peer typing indicator with avatar
                            if (isPeerTyping) {
                                item(key = "typing_indicator") {
                                    TypingIndicatorRow(peerName = receiverName)
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Gradient scrim separator ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                    )
                )
        )

        // ── Input Bar ────────────────────────────────────────────────────────
        ChatInputBar(
            text = inputText,
            onTextChange = {
                inputText = it
                viewModel.sendTypingEvent(receiverId)
            },
            onSend = {
                val msg = inputText.trim()
                if (msg.isNotBlank()) {
                    val currentUserName = context.getSharedPreferences(Constants.PREFS_NAME, android.content.Context.MODE_PRIVATE)
                        .getString(Constants.KEY_USER_NAME, "") ?: "Me"
                    val myUser = User(id = currentUserId, name = currentUserName)
                    viewModel.sendMessage(receiverId, msg, myUser)
                    inputText = ""
                }
            }
        )
    }
}

// ─── Message Bubble Composable ────────────────────────────────────────────────
@Composable
private fun MessageBubble(message: Message, isMyMessage: Boolean, isSending: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    ) {
        if (!isMyMessage) {
            AvatarInitials(name = message.sender?.name ?: "?", size = 28)
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
        ) {
            if (isMyMessage) {
                // Sender bubble — Indigo to Violet gradient, dimmed if sending
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 18.dp, topEnd = 18.dp,
                                bottomStart = 18.dp, bottomEnd = 4.dp
                            )
                        )
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    GradientIndigoStart.copy(alpha = if (isSending) 0.55f else 1f),
                                    Violet400.copy(alpha = if (isSending) 0.55f else 1f)
                                )
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.content,
                        style = TextStyle(fontSize = 14.5.sp, color = Color.White, lineHeight = 20.sp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val formattedTime = formatTime(message.createdAt)
                    if (isSending) {
                        Text(
                            text = "Sending…",
                            style = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f).copy(alpha = 0.6f))
                        )
                    } else {
                        Text(
                            text = "$formattedTime  ",
                            style = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        )
                        val isRead = message.isRead
                        Text(
                            text = if (isRead) "✓✓" else "✓",
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = if (isRead) SemanticSuccess else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                // Receiver bubble — Dark surface card with subtle border
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 18.dp, topEnd = 18.dp,
                                bottomStart = 4.dp, bottomEnd = 18.dp
                            )
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 0.8.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.content,
                        style = TextStyle(fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onBackground, lineHeight = 20.sp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                val formattedTime = formatTime(message.createdAt)
                Text(
                    text = formattedTime,
                    style = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                )
            }
        }
    }
}

// ─── Smart Date Separator Pill ───────────────────────────────────────────────
@Composable
private fun DateSeparator(label: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Subtle line behind the pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.background)
                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(999.dp))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Text(
                text = label,
                style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
            )
        }
    }
}

// ─── Typing Indicator Row with Peer Avatar ────────────────────────────────────
@Composable
private fun TypingIndicatorRow(peerName: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        AvatarInitials(name = peerName, size = 28)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(0.8.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val offsetY = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 140L)
                        offsetY.animateTo(
                            targetValue = -5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(300, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .offset(y = offsetY.value.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

// ─── Chat Input Bar Composable ────────────────────────────────────────────────
@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Attachment paperclip icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach File",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 0.8.dp,
                        brush = if (text.isNotBlank())
                            Brush.horizontalGradient(listOf(GradientIndigoStart.copy(alpha = 0.5f), Violet400.copy(alpha = 0.3f)))
                        else
                            Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onBackground),
                        maxLines = 4,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { inner ->
                            Box {
                                if (text.isEmpty()) {
                                    Text(
                                        text = "Type a message...",
                                        style = TextStyle(fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    if (text.isBlank()) {
                        Icon(
                            imageVector = Icons.Default.EmojiEmotions,
                            contentDescription = "Emoji",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button with Spring Press Animation
            val sendInteraction = remember { MutableInteractionSource() }
            val isPressed by sendInteraction.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.88f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "sendScale"
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        if (text.isNotBlank())
                            Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd))
                        else
                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                    )
                    .clickable(
                        enabled = text.isNotBlank(),
                        interactionSource = sendInteraction,
                        indication = null
                    ) { onSend() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = if (text.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

/**
 * Groups messages into date buckets ("Today", "Yesterday", or "MMM d, yyyy")
 */
private fun groupMessagesByDate(messages: List<Message>): Map<String, List<Message>> {
    val today     = LocalDate.now()
    val yesterday = today.minusDays(1)
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    return messages.groupBy { message ->
        if (message.createdAt.isBlank()) return@groupBy "Today"
        try {
            val date = Instant.parse(message.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
            when (date) {
                today     -> "Today"
                yesterday -> "Yesterday"
                else      -> formatter.format(date)
            }
        } catch (e: Exception) {
            "Today"
        }
    }
}

/**
 * Formats ISO timestamp to readable "h:mm a" format
 */
fun formatTime(isoString: String): String {
    if (isoString.isBlank()) return ""
    return try {
        val instant = Instant.parse(isoString)
        val zoneId  = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern("h:mm a").withZone(zoneId)
        formatter.format(instant)
    } catch (e: Exception) {
        ""
    }
}
