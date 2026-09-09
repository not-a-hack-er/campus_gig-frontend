/**
 * User.kt — Data model representing a CampusGig user.
 *
 * In CampusGig, every user is both a potential employer (can post gigs)
 * and a potential applicant (can apply to gigs). The 'role' field is just
 * a default preference set at registration.
 *
 * This class is used for:
 * - Auth responses (after login/register)
 * - Profile screen display
 * - Gig employer info (populated in API responses)
 * - Chat conversation list
 * - Applicant display in gig detail
 *
 * The @SerializedName annotation maps Kotlin field names to the JSON keys
 * returned by our MongoDB/Express backend (which uses camelCase).
 */
package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

data class User(
    // MongoDB document ID — stored as String in Kotlin (ObjectId in MongoDB)
    @SerializedName("_id")
    val id: String = "",

    // Basic identity
    val name: String = "",
    val email: String = "",

    /**
     * User's role on the platform.
     * Values: "student" | "employer"
     * Note: This is just a default — all users can do both.
     */
    val role: String = "",

    // Academic information — shown on profile and gig cards
    val college: String = "",
    val branch: String = "",           // e.g. "Computer Science", "Electronics"
    val yearOfStudy: String = "",      // e.g. "1st", "2nd", "3rd", "4th", "5th+"
    val graduationYear: Int = 0,       // e.g. 2026

    // Professional profile
    val bio: String = "",              // Short personal description
    val skills: List<String> = emptyList(), // e.g. ["Kotlin", "Android", "Figma"]
    val profilePicture: String = "",   // URL to profile photo (can be empty)

    // Portfolio & Resume links — displayed on profile as clickable URLs
    val portfolioLinks: List<String> = emptyList(), // Personal websites, Behance, etc.
    val githubProfile: String = "",    // GitHub username or full URL
    val linkedinProfile: String = "",  // LinkedIn URL
    val resumeUrl: String = "",        // URL / link to resume (PDF, Google Drive, etc.)

    // Reputation system — built up over time through completed gigs + reviews
    val rating: Double = 0.0,          // Average rating (0.0 – 5.0)
    val reviewCount: Int = 0,          // Total number of reviews received
    val completedGigsCount: Int = 0,   // Number of successfully completed gigs

    // Metadata
    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = ""
) {
    /**
     * Returns the user's initials for avatar display (up to 2 letters).
     * Example: "Akash Sharma" → "AS"
     */
    fun initials(): String {
        val parts = name.trim().split(" ")
        return if (parts.size >= 2) {
            "${parts.first().firstOrNull() ?: ""}${parts.last().firstOrNull() ?: ""}".uppercase()
        } else {
            name.take(2).uppercase()
        }
    }

    /**
     * Returns a formatted rating string like "4.8 ⭐ (23 reviews)".
     * Used on the profile screen.
     */
    fun ratingDisplay(): String {
        return if (reviewCount == 0) "No reviews yet"
        else "%.1f ⭐ (%d reviews)".format(rating, reviewCount)
    }
}
