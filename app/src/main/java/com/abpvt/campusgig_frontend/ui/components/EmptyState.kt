/**
 * EmptyState.kt — Composable shown when a list has no items.
 *
 * Redesigned from a Senior Product Designer perspective:
 * - Replaces generic material buttons with custom gradient capsule buttons.
 * - Supports an optional slot for recommendations/suggestions (so no page feels empty).
 * - Styled for high contrast headlines and clear subtexts.
 */
package com.abpvt.campusgig_frontend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abpvt.campusgig_frontend.ui.theme.CampusIndigo40
import com.abpvt.campusgig_frontend.ui.theme.CampusTeal40

/**
 * EmptyState — Displayed when there is no data to show.
 *
 * @param emoji         Unicode emoji that represents the empty context (e.g. "🔍", "💬")
 * @param title         Short headline (e.g. "No Gigs Found")
 * @param subtitle      Helpful explanation (e.g. "Try adjusting your filters")
 * @param actionLabel   Optional button text (e.g. "Browse Gigs"). Null = no button.
 * @param onAction      Callback when action button is tapped.
 * @param recommendationContent Optional slot to display suggestions or recommendations.
 */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    recommendationContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large emoji as visual cue
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bold headline
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Helpful subtitle
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Optional CTA button
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(CampusIndigo40, CampusTeal40)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onAction() }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Recommendation Slot
        if (recommendationContent != null) {
            Spacer(modifier = Modifier.height(28.dp))
            recommendationContent()
        }
    }
}
