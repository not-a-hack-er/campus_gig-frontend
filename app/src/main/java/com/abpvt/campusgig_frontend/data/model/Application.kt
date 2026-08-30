/**
 * Application.kt — Represents a student's application to a CampusGig gig.
 *
 * When a student finds a gig they want to work on, they submit an Application.
 * The application contains:
 * - A proposal (cover letter) explaining why they're the best fit
 * - Their expected budget (might differ from the gig's posted budget)
 *
 * The gig owner (employer) can then:
 * - View all applications for their gig
 * - Accept one application (triggers "in_progress" status on the gig)
 * - Reject others
 *
 * This model is populated with nested Gig and User objects from the backend.
 */
package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

data class Application(
    // MongoDB document ID
    @SerializedName("_id")
    val id: String = "",

    /**
     * The gig this application is for.
     * Populated by the backend — contains full Gig details.
     * Used when displaying "My Applications" (student view).
     */
    val gig: Gig? = null,

    /**
     * The student who submitted this application.
     * Populated by the backend — contains full User details.
     * Used when displaying applications for a specific gig (employer view).
     */
    val applicant: User? = null,

    /**
     * Cover letter / proposal text.
     * The student explains: why they want this gig, their relevant experience,
     * how they'll approach the project, and their timeline.
     */
    val proposal: String = "",

    /**
     * The student's expected budget in INR (₹).
     * May differ from the gig's posted budget — employer can negotiate.
     */
    val expectedBudget: Double = 0.0,

    /**
     * Application status — changes as the employer reviews and gig progresses.
     * Values: "pending" | "accepted" | "rejected" | "withdrawn" | "completed"
     * Default is "pending" when first submitted.
     */
    val status: String = "pending",

    /**
     * Deliverable work submission attached to this application when the student submits work.
     * Populated by the backend when the applicant calls submitWork().
     */
    val workSubmission: WorkSubmission? = null,

    // Timestamps
    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = ""
)
