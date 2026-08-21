/**
 * BottomNavBar.kt — The app's main bottom navigation bar.
 *
 * Redesigned from a Senior Product Designer perspective (inspired by Apple & Arc):
 * - Floating glassmorphic dock suspended above the screen with 32.dp rounded corners.
 * - Active state uses spring-based scale-up animations on the icons.
 * - Indicator uses a smooth sliding/fading neon active state dot.
 * - Soft cyan message count badges for conversations.
 */
package com.abpvt.campusgig_frontend.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.CampusIndigo40
import com.abpvt.campusgig_frontend.ui.theme.CampusTeal40

data class NavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun CampusGigBottomBar(
    navController: NavController,
    unreadMessageCount: Int = 0
) {
    val navItems = listOf(
        NavItem("Home",        Routes.HOME,           Icons.Filled.Home,   Icons.Outlined.Home),
        NavItem("Gigs",        Routes.GIG_LIST,       Icons.Filled.Work,   Icons.Outlined.Work),
        NavItem("Community",   Routes.COMMUNITY_LIST, Icons.Filled.Groups, Icons.Outlined.Groups),
        NavItem("Messages",    Routes.CHAT_LIST,      Icons.AutoMirrored.Filled.Chat,   Icons.AutoMirrored.Outlined.Chat),
        NavItem("Profile",     Routes.PROFILE,        Icons.Filled.Person, Icons.Outlined.Person)
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Transparent glass backdrop with 32dp corners
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                )
            ),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route ||
                        (item.route == Routes.GIG_LIST && currentRoute?.startsWith("gig_") == true) ||
                        (item.route == Routes.COMMUNITY_LIST && currentRoute?.startsWith("community_") == true) ||
                        (item.route == Routes.CHAT_LIST && currentRoute?.startsWith("chat") == true)

                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

                    // Spring animation for icon scale
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "NavIconScale"
                    )

                    // Spring animation for text scaling
                    val textScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "NavTextScale"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isSelected) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            .padding(vertical = 6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .scale(iconScale)
                        ) {
                            if (item.route == Routes.CHAT_LIST && unreadMessageCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = CampusTeal40,
                                            contentColor = Color.Black
                                        ) {
                                            Text(
                                                text = if (unreadMessageCount > 99) "99+" else unreadMessageCount.toString(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) activeColor else inactiveColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) activeColor else inactiveColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                letterSpacing = 0.1.sp
                            ),
                            modifier = Modifier.scale(textScale),
                            color = if (isSelected) activeColor else inactiveColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Selection spring-slide-like dot indicator
                        Box(
                            modifier = Modifier
                                .size(width = if (isSelected) 12.dp else 0.dp, height = 3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(if (isSelected) activeColor else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
