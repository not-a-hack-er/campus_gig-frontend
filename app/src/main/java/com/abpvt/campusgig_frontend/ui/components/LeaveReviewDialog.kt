package com.abpvt.campusgig_frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoEnd
import com.abpvt.campusgig_frontend.ui.theme.GradientIndigoStart
import com.abpvt.campusgig_frontend.ui.theme.SemanticError
import com.abpvt.campusgig_frontend.ui.theme.SemanticErrorBg
import com.abpvt.campusgig_frontend.ui.theme.SemanticSuccess
import com.abpvt.campusgig_frontend.ui.theme.SemanticWarning
import kotlinx.coroutines.launch

@Composable
fun LeaveReviewDialog(
    gigId: String,
    gigTitle: String,
    targetUserId: String,
    targetUserName: String,
    onDismiss: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val api = (context.applicationContext as CampusGigApplication).apiService
    val scope = rememberCoroutineScope()

    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSubmitted) "Review Submitted!" else "Rate & Review",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }

                if (isSubmitted) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SemanticSuccess.copy(alpha = 0.15f))
                            .border(1.5.dp, SemanticSuccess, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SemanticSuccess, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Thank you for your feedback!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your review for \"$gigTitle\" has been recorded and added to $targetUserName's verified profile.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                            .clickable {
                                onDismiss()
                                onSubmitSuccess()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Done", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "How was your experience working with $targetUserName on \"$gigTitle\"?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5-Star Interactive Rating Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { star ->
                            val isSelected = star <= rating
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { rating = star },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "$star Stars",
                                    tint = if (isSelected) SemanticWarning else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (rating) {
                            5 -> "⭐⭐⭐⭐⭐ Outstanding work"
                            4 -> "⭐⭐⭐⭐ Great job"
                            3 -> "⭐⭐⭐ Average experience"
                            2 -> "⭐⭐ Needs improvement"
                            else -> "⭐ Poor experience"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = SemanticWarning
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Written Comment Field
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        placeholder = { Text("Write your review (optional)...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 3
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SemanticErrorBg, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(errorMessage!!, color = SemanticError, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(GradientIndigoStart, GradientIndigoEnd)))
                            .clickable(enabled = !isLoading) {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val body = mapOf(
                                            "rating" to rating,
                                            "comment" to comment.trim(),
                                            "gigId" to gigId
                                        )
                                        val res = api.submitReview(targetUserId, body)
                                        if (res.isSuccessful) {
                                            isSubmitted = true
                                        } else {
                                            val errStr = res.errorBody()?.string() ?: res.message()
                                            errorMessage = try {
                                                org.json.JSONObject(errStr).optString("message", "Failed to submit review")
                                            } catch (e: Exception) {
                                                "Failed to submit review (${res.code()})"
                                            }
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = e.localizedMessage ?: "Network error"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Submit Review", color = Color.White, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
