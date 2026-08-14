/**
 * AppNavGraph.kt — The central navigation controller for CampusGig.
 *
 * This file does TWO key things:
 *
 * 1. SCAFFOLD WRAPPER:
 *    Wraps the entire app in a Scaffold with the BottomNavBar.
 *    The bottom bar is shown on "main" screens (Home, Gigs, Community, Chat, Profile).
 *    It is HIDDEN on sub-screens (GigDetail, Chat, Login, etc.) so it doesn't overlap.
 *
 * 2. NAVHOST:
 *    Defines every possible screen in the app and the route string that activates it.
 *    When navController.navigate("gig_list") is called anywhere in the app,
 *    Navigation Compose looks this up here and shows GigListScreen.
 *
 * PARAMETER EXTRACTION:
 * For routes like "gig_detail/{gigId}", Navigation Compose automatically extracts
 * the {gigId} segment and makes it available via navBackStackEntry.arguments.
 *
 * BOTTOM BAR VISIBILITY:
 * We compute currentRoute and check if it's a "main" route to show/hide the bottom bar.
 * This prevents the bar from showing on Login, GigDetail, ChatScreen, etc.
 */
package com.abpvt.campusgig_frontend.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.abpvt.campusgig_frontend.features.applications.MyApplicationsScreen
import com.abpvt.campusgig_frontend.features.gigs.MyGigsScreen
import com.abpvt.campusgig_frontend.features.gigs.GigApplicationsScreen
import com.abpvt.campusgig_frontend.features.auth.LoginScreen
import com.abpvt.campusgig_frontend.features.auth.RegisterScreen
import com.abpvt.campusgig_frontend.features.chat.ChatListScreen
import com.abpvt.campusgig_frontend.features.chat.ChatScreen
import com.abpvt.campusgig_frontend.features.communities.CommunityDetailScreen
import com.abpvt.campusgig_frontend.features.communities.CommunityListScreen
import com.abpvt.campusgig_frontend.features.communities.CreateCommunityScreen
import com.abpvt.campusgig_frontend.features.gigs.CreateGigScreen
import com.abpvt.campusgig_frontend.features.gigs.GigDetailScreen
import com.abpvt.campusgig_frontend.features.gigs.GigListScreen
import com.abpvt.campusgig_frontend.features.home.HomeScreen
import com.abpvt.campusgig_frontend.features.notifications.NotificationScreen
import com.abpvt.campusgig_frontend.features.chat.MediaViewerScreen
import com.abpvt.campusgig_frontend.features.home.SearchScreen
import com.abpvt.campusgig_frontend.features.communities.CreatePostScreen
import com.abpvt.campusgig_frontend.features.communities.PostDetailScreen
import com.abpvt.campusgig_frontend.features.profile.EditProfileScreen
import com.abpvt.campusgig_frontend.features.profile.ProfileScreen
import com.abpvt.campusgig_frontend.features.profile.PublicProfileScreen
import com.abpvt.campusgig_frontend.features.profile.ReviewsScreen
import com.abpvt.campusgig_frontend.features.profile.SettingsScreen
import com.abpvt.campusgig_frontend.features.profile.ProfileSetupPromptScreen
import com.abpvt.campusgig_frontend.features.notifications.NotificationSettingsScreen
import com.abpvt.campusgig_frontend.features.splash.SplashScreen
import com.abpvt.campusgig_frontend.ui.components.CampusGigBottomBar

/**
 * AppNavGraph — The root composable that hosts the entire navigation structure.
 *
 * @param navController      The NavHostController created in MainActivity.
 * @param startDestination   Either Routes.LOGIN or Routes.HOME (based on session state).
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    isLoggedIn: Boolean = false
) {
    // The routes on which the bottom bar SHOULD appear
    val mainRoutes = setOf(
        Routes.HOME,
        Routes.GIG_LIST,
        Routes.COMMUNITY_LIST,
        Routes.CHAT_LIST,
        Routes.PROFILE
    )

    // Observe current route to decide whether to show bottom bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if bottom bar should be visible on current screen
    val showBottomBar = currentRoute in mainRoutes

    Scaffold(
        bottomBar = {
            // Only render the bottom bar on the 5 main screens
            if (showBottomBar) {
                CampusGigBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ── Splash Screen (start destination) ──────────────────────────────────
            composable(Routes.SPLASH) {
                SplashScreen(
                    navController = navController,
                    isLoggedIn    = isLoggedIn
                )
            }

            // ── Auth Screens ──────────────────────────────────────────────────────
            composable(Routes.LOGIN) {
                LoginScreen(navController = navController)
            }

            composable(Routes.REGISTER) {
                RegisterScreen(navController = navController)
            }

            // ── Main Screens (with bottom bar) ────────────────────────────────
            composable(Routes.HOME) {
                HomeScreen(navController = navController)
            }

            composable(Routes.GIG_LIST) {
                GigListScreen(navController = navController)
            }

            composable(Routes.COMMUNITY_LIST) {
                CommunityListScreen(navController = navController)
            }

            composable(Routes.CHAT_LIST) {
                ChatListScreen(navController = navController)
            }

            composable(Routes.PROFILE) {
                ProfileScreen(navController = navController)
            }

            // ── Gig Sub-Screens ───────────────────────────────────────────────

            /**
             * GigDetailScreen — receives {gigId} as a path parameter.
             * navArgument() tells Navigation to extract it as a String.
             * Access it via: navBackStackEntry.arguments?.getString("gigId")
             */
            composable(
                route = Routes.GIG_DETAIL,
                arguments = listOf(
                    navArgument(Routes.ARG_GIG_ID) { type = NavType.StringType }
                )
            ) { navBackStackEntry ->
                // Extract the gigId parameter from the navigation arguments
                val gigId = navBackStackEntry.arguments?.getString(Routes.ARG_GIG_ID) ?: return@composable
                GigDetailScreen(navController = navController, gigId = gigId)
            }

            composable(Routes.CREATE_GIG) {
                CreateGigScreen(navController = navController)
            }

            composable(Routes.MY_APPLICATIONS) {
                MyApplicationsScreen(navController = navController)
            }

            composable(Routes.MY_GIGS) {
                MyGigsScreen(navController = navController)
            }

            composable(
                route = Routes.GIG_APPLICATIONS,
                arguments = listOf(navArgument(Routes.ARG_GIG_ID) { type = NavType.StringType })
            ) { navBackStackEntry ->
                val gigId = navBackStackEntry.arguments?.getString(Routes.ARG_GIG_ID) ?: return@composable
                GigApplicationsScreen(navController = navController, gigId = gigId)
            }

            // ── Chat Sub-Screens ──────────────────────────────────────────────
            /**
             * ChatScreen — receives both {receiverId} and {receiverName}.
             * Note: receiverName might contain %20 (URL-encoded spaces).
             * Navigation Compose automatically URL-decodes String arguments.
             */
            composable(
                route = Routes.CHAT,
                arguments = listOf(
                    navArgument(Routes.ARG_RECEIVER_ID)   { type = NavType.StringType },
                    navArgument(Routes.ARG_RECEIVER_NAME) { type = NavType.StringType },
                    navArgument("gigTitle")  { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("gigBudget") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { navBackStackEntry ->
                val receiverId   = navBackStackEntry.arguments?.getString(Routes.ARG_RECEIVER_ID)   ?: return@composable
                val receiverName = navBackStackEntry.arguments?.getString(Routes.ARG_RECEIVER_NAME)?.replace("%20", " ") ?: "User"
                val gigTitle     = navBackStackEntry.arguments?.getString("gigTitle")?.replace("%20", " ")
                val gigBudget    = navBackStackEntry.arguments?.getString("gigBudget")?.replace("%20", " ")
                ChatScreen(
                    navController  = navController,
                    receiverId     = receiverId,
                    receiverName   = receiverName,
                    gigTitle       = gigTitle,
                    gigBudget      = gigBudget
                )
            }

            // ── Community Sub-Screens ──────────────────────────────────────────
            composable(
                route = Routes.COMMUNITY_DETAIL,
                arguments = listOf(
                    navArgument(Routes.ARG_COMMUNITY_ID) { type = NavType.StringType }
                )
            ) { navBackStackEntry ->
                val communityId = navBackStackEntry.arguments?.getString(Routes.ARG_COMMUNITY_ID) ?: return@composable
                CommunityDetailScreen(navController = navController, communityId = communityId)
            }

            composable(Routes.CREATE_COMMUNITY) {
                CreateCommunityScreen(navController = navController)
            }

            // ── Home Sub-Screens ───────────────────────────────────────────────
            composable(Routes.SEARCH) {
                SearchScreen(navController = navController)
            }

            // ── Notification Screen ────────────────────────────────────────────
            composable(Routes.NOTIFICATIONS) {
                NotificationScreen(navController = navController)
            }

            // ── Profile Sub-Screens ────────────────────────────────────────────
            composable(Routes.EDIT_PROFILE) {
                EditProfileScreen(navController = navController)
            }

            composable(
                route = Routes.PUBLIC_PROFILE,
                arguments = listOf(
                    navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }
                )
            ) { navBackStackEntry ->
                val userId = navBackStackEntry.arguments?.getString(Routes.ARG_USER_ID) ?: return@composable
                PublicProfileScreen(navController = navController, userId = userId)
            }

            composable(
                route = Routes.REVIEWS,
                arguments = listOf(
                    navArgument(Routes.ARG_USER_ID) { type = NavType.StringType }
                )
            ) { navBackStackEntry ->
                val userId = navBackStackEntry.arguments?.getString(Routes.ARG_USER_ID) ?: return@composable
                ReviewsScreen(navController = navController, userId = userId)
            }

            composable(
                route = Routes.MEDIA_VIEWER,
                arguments = listOf(
                    navArgument(Routes.ARG_MEDIA_URL) { type = NavType.StringType }
                )
            ) { navBackStackEntry ->
                val mediaUrl = navBackStackEntry.arguments?.getString(Routes.ARG_MEDIA_URL)?.replace("%2F", "/") ?: return@composable
                MediaViewerScreen(navController = navController, mediaUrl = mediaUrl)
            }

            // ── Settings Screens ────────────────────────────────────────────────────
            composable(Routes.SETTINGS) {
                SettingsScreen(navController = navController)
            }

            composable(Routes.NOTIFICATION_SETTINGS) {
                NotificationSettingsScreen(navController = navController)
            }

            // ── Onboarding ───────────────────────────────────────────────────────────
            composable(Routes.PROFILE_SETUP) {
                ProfileSetupPromptScreen(navController = navController)
            }

            // ── Community Post Screens ───────────────────────────────────────────────
            composable(
                route = Routes.CREATE_POST,
                arguments = listOf(navArgument("communityId") { type = NavType.StringType })
            ) { back ->
                val communityId = back.arguments?.getString("communityId") ?: return@composable
                CreatePostScreen(navController = navController, communityId = communityId)
            }

            composable(
                route = Routes.POST_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_POST_ID) { type = NavType.StringType })
            ) { back ->
                val postId = back.arguments?.getString(Routes.ARG_POST_ID) ?: return@composable
                PostDetailScreen(navController = navController, postId = postId)
            }
        }
    }
}
