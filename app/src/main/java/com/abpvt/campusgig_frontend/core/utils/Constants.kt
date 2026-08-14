/**
 * Constants.kt — Central configuration and constant values for CampusGig.
 *
 * This file is the single source of truth for:
 * - API base URL and Socket server URL
 * - SharedPreferences key names (for session storage)
 * - Domain constants (roles, statuses, categories)
 * - Socket.IO event names
 * - Pagination defaults
 *
 * Why a separate Constants file?
 * If you ever need to change the server URL (e.g. from dev to prod),
 * you only change it here instead of hunting through multiple files.
 */
package com.abpvt.campusgig_frontend.core.utils

import com.abpvt.campusgig_frontend.BuildConfig

object Constants {

    // ─── API Configuration ────────────────────────────────────────────────────
    /**
     * BASE_URL: The root URL for all REST API calls via Retrofit.
     *
     * This value is injected at compile time from BuildConfig, which reads from
     * keystore.properties. Configure environment-specific URLs there, not here.
     *
     * For local dev on emulator:  http://10.0.2.2:5000/api/  (set in keystore.properties)
     * For local dev on device:    http://192.168.1.X:5000/api/
     * For production:             https://api.campusgig.com/api/
     */
    val BASE_URL: String get() = BuildConfig.BASE_URL

    /**
     * SOCKET_URL: The root URL for Socket.IO WebSocket connections.
     * Note: No /api/ suffix — Socket.IO connects to the server root.
     * Also injected from BuildConfig / keystore.properties.
     */
    val SOCKET_URL: String get() = BuildConfig.SOCKET_URL

    // ─── SharedPreferences ────────────────────────────────────────────────────
    /**
     * PREFS_NAME: The name of the SharedPreferences file.
     * All session-related data is stored here persistently on the device.
     */
    const val PREFS_NAME = "campus_gig_prefs"

    // Keys used to store/retrieve values in SharedPreferences
    const val KEY_TOKEN       = "auth_token"      // JWT authentication token
    const val KEY_USER_ID     = "user_id"         // Logged-in user's MongoDB _id
    const val KEY_USER_ROLE   = "user_role"       // "student" or "employer"
    const val KEY_USER_NAME   = "user_name"       // Cached display name
    const val KEY_USER_EMAIL  = "user_email"      // Cached email

    // ─── Pagination ───────────────────────────────────────────────────────────
    /**
     * Default number of items to load per page in list screens.
     * Lower = faster initial load; Higher = fewer network calls.
     */
    const val DEFAULT_PAGE_SIZE = 10

    // ─── User Roles ───────────────────────────────────────────────────────────
    /**
     * In CampusGig, every user can be BOTH a student and employer simultaneously.
     * The role selected at registration is a default — they can always switch context.
     */
    const val ROLE_STUDENT  = "student"   // Default — can apply to gigs
    const val ROLE_EMPLOYER = "employer"  // Can post gigs and review applicants

    // ─── Gig Statuses ─────────────────────────────────────────────────────────
    /**
     * Status lifecycle: open → in_progress → closed/completed
     * - open:        Gig is visible and accepting applications
     * - in_progress: An applicant has been selected, work is ongoing
     * - closed:      Gig is no longer accepting applications (or completed)
     */
    const val GIG_STATUS_OPEN        = "open"
    const val GIG_STATUS_CLOSED      = "closed"
    const val GIG_STATUS_IN_PROGRESS = "in_progress"
    const val GIG_STATUS_COMPLETED   = "completed"

    // ─── Application Statuses ─────────────────────────────────────────────────
    /**
     * Status lifecycle: pending → accepted OR rejected
     * - pending:  Just submitted, waiting for gig owner to review
     * - accepted: Gig owner selected this applicant
     * - rejected: Gig owner chose someone else
     */
    const val APPLICATION_STATUS_PENDING  = "pending"
    const val APPLICATION_STATUS_ACCEPTED = "accepted"
    const val APPLICATION_STATUS_REJECTED = "rejected"

    // ─── Gig Categories ───────────────────────────────────────────────────────
    /**
     * Predefined categories shown as filter chips on the Gig List screen.
     * These should match what the backend accepts for the 'category' field.
     */
    val GIG_CATEGORIES = listOf(
        "All",
        "Android Dev",
        "Web Dev",
        "UI/UX Design",
        "Data Science",
        "Content Writing",
        "Video Editing",
        "Photography",
        "Graphic Design",
        "Machine Learning",
        "Open Source",
        "Other"
    )

    // ─── Community Categories ─────────────────────────────────────────────────
    /**
     * Categories for community groups. Students can create communities
     * around any of these topics.
     */
    val COMMUNITY_CATEGORIES = listOf(
        "Android Development",
        "Web Development",
        "Artificial Intelligence",
        "UI/UX Design",
        "Open Source",
        "Hackathons",
        "Placement Prep",
        "Entrepreneurship",
        "Data Science",
        "Cybersecurity",
        "Game Dev",
        "General"
    )

    // ─── Socket.IO Event Names ────────────────────────────────────────────────
    /**
     * These event names MUST match the server-side Socket.IO event names exactly.
     * If the backend changes an event name, update it here to keep them in sync.
     *
     * Emit events (client → server):
     *   SOCKET_JOIN_ROOM     : Join a private chat room (roomId = sorted user IDs)
     *   SOCKET_SEND_MESSAGE  : Send a chat message
     *   SOCKET_TYPING        : Notify the other user we're typing
     *
     * Listen events (server → client):
     *   SOCKET_NEW_MESSAGE   : A new message arrived in the current room
     *   SOCKET_TYPING        : The other user is typing
     *   SOCKET_ONLINE_USERS  : List of currently connected users
     */
    const val SOCKET_JOIN_ROOM    = "join_room"
    const val SOCKET_SEND_MESSAGE = "send_message"
    const val SOCKET_TYPING       = "typing"
    const val SOCKET_NEW_MESSAGE  = "new_message"
    const val SOCKET_ONLINE_USERS = "online_users"
}
