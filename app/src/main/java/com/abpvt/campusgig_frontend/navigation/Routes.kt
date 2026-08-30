/**
 * Routes.kt — Type-safe navigation route constants for CampusGig. v2.0
 *
 * WHY A ROUTES FILE?
 * In Jetpack Navigation Compose, routes are plain strings.
 * Defining them as constants prevents typos — instead of:
 *   navController.navigate("gig_detail/abc123")  // easy to typo
 * We use:
 *   navController.navigate(Routes.gigDetail(gigId))  // type-safe
 *
 * Routes with parameters:
 * - {gigId}        → variable part in the route pattern
 * - Routes.gigDetail("abc123") → builds "gig_detail/abc123"
 *
 * The NavGraph uses ARG_ constants to extract parameters from the back stack.
 */
package com.abpvt.campusgig_frontend.navigation

object Routes {

    // ── Splash ────────────────────────────────────────────────────────────────
    const val SPLASH = "splash"

    // ── Auth ──────────────────────────────────────────────────────────────────
    const val LOGIN           = "login"
    const val REGISTER        = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    // ── Main Screens (shown in BottomNavBar) ──────────────────────────────────
    const val HOME           = "home"
    const val GIG_LIST       = "gig_list"
    const val COMMUNITY_LIST = "community_list"
    const val CHAT_LIST      = "chat_list"
    const val PROFILE        = "profile"

    // ── Home Sub-Screens ──────────────────────────────────────────────────────
    const val SEARCH = "search"

    // ── Settings ─────────────────────────────────────────────────────────────
    const val SETTINGS                = "settings"
    const val NOTIFICATION_SETTINGS   = "notification_settings"

    // ── Onboarding ────────────────────────────────────────────────────────────
    const val PROFILE_SETUP           = "profile_setup"

    // ── Gig Screens ───────────────────────────────────────────────────────────
    /** Route PATTERN for gig detail — {gigId} is the parameter placeholder */
    const val GIG_DETAIL     = "gig_detail/{gigId}"
    const val CREATE_GIG     = "create_gig"

    /**
     * Builds the gig detail navigation route with a specific ID.
     * Usage: navController.navigate(Routes.gigDetail("abc123"))
     * Result: "gig_detail/abc123"
     */
    fun gigDetail(gigId: String) = "gig_detail/$gigId"

    // ── Application Screens ───────────────────────────────────────────────────
    const val MY_APPLICATIONS   = "my_applications"
    const val APPLICATION_DETAIL = "application_detail/{applicationId}"
    fun applicationDetail(applicationId: String) = "application_detail/$applicationId"
    const val ARG_APPLICATION_ID = "applicationId"

    // ── My Gigs Screen ────────────────────────────────────────────────────────
    const val MY_GIGS = "my_gigs"
    const val GIG_APPLICATIONS = "gig_applications/{gigId}"
    fun gigApplications(gigId: String) = "gig_applications/$gigId"

    // ── Community Post Screens ────────────────────────────────────────────────
    const val CREATE_POST = "create_post/{communityId}"
    fun createPost(communityId: String) = "create_post/$communityId"

    const val POST_DETAIL = "post_detail/{postId}"
    fun postDetail(postId: String) = "post_detail/$postId"

    const val ARG_POST_ID = "postId"

    // ── Chat Screens ──────────────────────────────────────────────────────────
    /**
     * Chat route with receiver info and optional gig context (gigTitle, gigBudget).
     */
    const val CHAT = "chat/{receiverId}/{receiverName}?gigTitle={gigTitle}&gigBudget={gigBudget}&gigId={gigId}"

    fun chat(
        receiverId: String,
        receiverName: String,
        gigTitle: String? = null,
        gigBudget: String? = null,
        gigId: String? = null
    ): String {
        val encodedName = receiverName.replace(" ", "%20")
        val titleQuery  = if (!gigTitle.isNullOrBlank()) "gigTitle=${gigTitle.replace(" ", "%20")}" else ""
        val budgetQuery = if (!gigBudget.isNullOrBlank()) "gigBudget=${gigBudget.replace(" ", "%20")}" else ""
        val idQuery     = if (!gigId.isNullOrBlank()) "gigId=$gigId" else ""
        val queryString = listOf(titleQuery, budgetQuery, idQuery).filter { it.isNotBlank() }.joinToString("&")
        return if (queryString.isNotBlank()) "chat/$receiverId/$encodedName?$queryString" else "chat/$receiverId/$encodedName"
    }

    /** Media viewer for full-screen image/video in chat */
    const val MEDIA_VIEWER = "media_viewer/{mediaUrl}"
    fun mediaViewer(mediaUrl: String) = "media_viewer/${mediaUrl.replace("/", "%2F")}"

    // ── Community Screens ─────────────────────────────────────────────────────
    const val COMMUNITY_DETAIL  = "community_detail/{communityId}"
    const val CREATE_COMMUNITY  = "create_community"

    fun communityDetail(communityId: String) = "community_detail/$communityId"

    // ── Notification Screen ───────────────────────────────────────────────────
    const val NOTIFICATIONS = "notifications"

    // ── Profile Screens ───────────────────────────────────────────────────────
    const val EDIT_PROFILE = "edit_profile"

    /** Public profile view for any user (by userId) */
    const val PUBLIC_PROFILE = "public_profile/{userId}"
    fun publicProfile(userId: String) = "public_profile/$userId"

    /** Reviews screen — shows all reviews for a userId */
    const val REVIEWS = "reviews/{userId}"
    fun reviews(userId: String) = "reviews/$userId"

    // ── Navigation Argument Keys ──────────────────────────────────────────────
    // These keys are used in NavGraph composable {} blocks to extract params
    const val ARG_GIG_ID        = "gigId"
    const val ARG_COMMUNITY_ID  = "communityId"
    const val ARG_RECEIVER_ID   = "receiverId"
    const val ARG_RECEIVER_NAME = "receiverName"
    const val ARG_MEDIA_URL     = "mediaUrl"
    const val ARG_USER_ID       = "userId"
}
