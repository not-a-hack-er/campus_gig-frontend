/**
 * Community.kt — Data model for CampusGig interest-based communities.
 *
 * Communities are one of CampusGig's key differentiators from pure freelancing
 * platforms. They allow students to:
 * - Connect with peers who share similar interests (Android Dev, AI, etc.)
 * - Post discussion threads, questions, and resource links
 * - Network and find collaborators for hackathons and projects
 * - Build a professional reputation within their domain
 *
 * Any student can create a community. The creator becomes the moderator.
 * Communities can be public (anyone can join) or private (invite only).
 */
package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

data class Community(
    // MongoDB document ID
    @SerializedName("_id")
    val id: String = "",

    // Community identity
    val name: String = "",             // e.g. "Android Developers @ IIT Bombay"
    val description: String = "",      // What this community is about
    val category: String = "",         // From Constants.COMMUNITY_CATEGORIES
    val coverImage: String = "",       // URL for the community header image

    /**
     * Creator/moderator of the community.
     * Populated by backend with full User details.
     */
    val creator: User? = null,

    /**
     * Member list — all users who have joined.
     * NOTE: This may be truncated for large communities (backend may only return count).
     * For member count, always use 'memberCount'.
     */
    val members: List<User> = emptyList(),
    val memberCount: Int = 0,          // Accurate total member count from backend

    /**
     * Privacy setting.
     * false = Public (default) — anyone can view and join
     * true  = Private — creator must approve join requests
     */
    val isPrivate: Boolean = false,

    // Timestamps
    @SerializedName("createdAt")
    val createdAt: String = ""
)
