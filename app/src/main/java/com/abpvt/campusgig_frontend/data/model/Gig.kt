/**
 * Gig.kt — Data model for a CampusGig gig (project/task posting).
 *
 * A gig is the core entity of CampusGig — it's the "project" that
 * students post when they need help, and what other students browse and apply to.
 *
 * Lifecycle:
 * 1. Employer posts gig (status: "open")
 * 2. Students apply (status stays "open", applicationsCount increases)
 * 3. Employer selects applicant (status: "in_progress")
 * 4. Worker submits deliverable (status: "work_submitted") — OTP sent to employer
 * 5. Employer enters OTP (status: "completed") — OR auto-completed after 3 days
 *
 * The 'employer' field is populated by the backend when fetching gig lists
 * (using Mongoose's .populate() method). If null, use fallback display.
 */
package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

data class Gig(
    // MongoDB document ID
    @SerializedName("_id")
    val id: String = "",

    // Core gig details — filled in by the poster
    val title: String = "",            // Short gig title (e.g. "Build Android Login Screen")
    val description: String = "",      // Full description with requirements and context
    val category: String = "",         // Category from predefined list (see Constants.GIG_CATEGORIES)
    val budget: Double = 0.0,          // Budget in Indian Rupees (₹)
    val duration: String = "",         // Expected timeline (e.g. "1 week", "3 days")
    val location: String = "",         // "Remote" or specific city/campus
    val deadline: String = "",         // Deadline date as ISO string (e.g. "2026-07-01T00:00:00Z")

    // Skills required — used for matching and filtering
    val skills: List<String> = emptyList(), // e.g. ["Kotlin", "Jetpack Compose", "Firebase"]
    val tags: List<String> = emptyList(),   // Extra searchable keywords

    /**
     * Gig status — controls visibility and what actions are available.
     * Values: "open" | "in_progress" | "work_submitted" | "completed" | "cancelled"
     *
     * State machine (backend-enforced):
     *   open → in_progress (employer accepts applicant)
     *   in_progress → work_submitted (worker submits deliverable)
     *   work_submitted → completed (employer enters OTP, or auto after 3 days)
     *   open | in_progress → cancelled (employer cancels)
     */
    val status: String = "open",

    /**
     * Employer info — populated by backend.
     * This is a nested User object (result of Mongoose populate).
     * Can be null if the gig was fetched without population.
     */
    val employer: User? = null,

    /**
     * The MongoDB _id of the accepted applicant (worker).
     * Set by the backend when an application is accepted.
     * Used to identify the worker in the Submit Work and OTP flows.
     * Null if no applicant has been accepted yet.
     */
    val acceptedApplicant: String? = null,

    // Work submission details (populated when status == "work_submitted").
    // The accepted worker sets these via POST /gigs/:id/submit-work so the
    // employer can review the deliverable before entering the completion OTP.
    val submittedWorkUrl: String = "",
    val submittedWorkNote: String = "",

    // Application tracking
    val applicationsCount: Int = 0,    // Total number of applications received

    // Timestamps
    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = ""
) {
    /**
     * Formats the budget for display.
     * Example: 1500.0 → "₹1,500"
     */
    fun formattedBudget(): String {
        return "₹${budget.toInt().toString().reversed().chunked(3).joinToString(",").reversed()}"
    }

    /** Returns true if the gig is accepting new applications. */
    fun isOpen(): Boolean = status.equals("open", ignoreCase = true)

    /** Returns true if a worker is actively working on the gig. */
    fun isInProgress(): Boolean = status.equals("in_progress", ignoreCase = true)

    /** Returns true if the worker has submitted their work and is awaiting OTP confirmation. */
    fun isWorkSubmitted(): Boolean = status.equals("work_submitted", ignoreCase = true)

    /** Returns true if the gig has been fully completed. */
    fun isCompleted(): Boolean = status.equals("completed", ignoreCase = true)
}
