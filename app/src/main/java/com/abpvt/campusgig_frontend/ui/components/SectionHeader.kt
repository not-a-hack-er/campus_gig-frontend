/**
 * SectionHeader.kt — Reusable section title composable.
 *
 * Used to create consistent visual hierarchy across Home, Profile, and
 * Community screens. Shows a bold title with an optional "See All" action link.
 *
 * Example usage:
 * ```
 * SectionHeader(
 *     title = "Recent Gigs",
 *     actionLabel = "See All",
 *     onAction = { navController.navigate(Routes.GIG_LIST) }
 * )
 * ```
 */
package com.abpvt.campusgig_frontend.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * @param title         The section heading text (e.g. "Featured Gigs")
 * @param actionLabel   Optional "See All" style button text. Null = hide button.
 * @param onAction      Called when the action button is tapped.
 */
@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // "See All" button — only shown if actionLabel is provided
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
