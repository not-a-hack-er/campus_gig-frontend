package com.abpvt.campusgig_frontend.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoMid
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning

private val FEEDBACK_TYPES = listOf(
    "BUG_REPORT"      to "🐛 Bug Report",
    "FEATURE_REQUEST" to "💡 Feature Request",
    "APP_RATING"      to "⭐ App Rating",
    "GENERAL_HELP"    to "❓ Help / Support"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HelpFeedbackBottomSheet(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (type: String, rating: Int?, message: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("GENERAL_HELP") }
    var selectedRating by remember { mutableIntStateOf(5) }
    var messageText by remember { mutableStateOf("") }
    var messageError by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "Help & Feedback 💬",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "We'd love to hear your thoughts! Report a bug, suggest a feature, or leave us a rating.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(18.dp))

            // Category Chips
            Text(
                "Feedback Category",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FEEDBACK_TYPES.forEach { (typeKey, label) ->
                    val isSelected = selectedType == typeKey
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                ),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { selectedType = typeKey }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Interactive Star Rating
            Text(
                "App Rating",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..5).forEach { star ->
                    val isFilled = star <= selectedRating
                    Icon(
                        imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "$star Stars",
                        tint = if (isFilled) SemanticWarning else MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { selectedRating = star }
                            )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    when (selectedRating) {
                        5 -> "Excellent! 🔥"
                        4 -> "Good! 👍"
                        3 -> "Average 😐"
                        2 -> "Needs Improvement 😕"
                        else -> "Poor 👎"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                    color = SemanticWarning
                )
            }

            Spacer(Modifier.height(18.dp))

            // Message TextField
            OutlinedTextField(
                value = messageText,
                onValueChange = {
                    messageText = it
                    if (it.trim().length >= 5) messageError = ""
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                label = { Text("Your Feedback / Details *", style = MaterialTheme.typography.labelMedium) },
                placeholder = { Text("Describe what happened, your feature idea, or what we can improve…", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                isError = messageError.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = SemanticError,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            if (messageError.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(messageError, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = SemanticError)
            }

            Spacer(Modifier.height(24.dp))

            // Submit Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(GradientIndigoStart, GradientIndigoMid, GradientIndigoEnd)
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (messageText.trim().length < 5) {
                                messageError = "Please write at least 5 characters"
                            } else if (!isSubmitting) {
                                onSubmit(selectedType, selectedRating, messageText.trim())
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        "Submit Feedback →",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
