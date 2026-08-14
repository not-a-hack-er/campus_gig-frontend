package com.abpvt.campusgig_frontend.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abpvt.campusgig_frontend.data.model.User

/**
 * UserAvatarRow — A standard component showing a circular avatar and user info.
 *
 * @param user The [User] data to display.
 * @param modifier Optional layout modifier.
 * @param avatarSize Size of the initials avatar in dp.
 */
@Composable
fun UserAvatarRow(
    user: User,
    modifier: Modifier = Modifier,
    avatarSize: Int = 40
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarInitials(name = user.name, size = avatarSize)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = user.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (user.college.isNotBlank()) {
                Text(
                    text = user.college,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
