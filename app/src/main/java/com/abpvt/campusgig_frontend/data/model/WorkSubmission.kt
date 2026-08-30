/**
 * WorkSubmission.kt — Represents a worker's submitted deliverable for a completed gig.
 *
 * When the accepted applicant finishes their work, they submit via POST /api/gigs/:id/submit-work.
 * The backend stores the submission details on the Application document.
 * This model is the frontend representation of that nested object.
 *
 * The employer can see this in the GigDetailScreen when the gig is in "work_submitted" state.
 */
package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

data class WorkSubmission(
    /**
     * URL to the deliverable — could be a GitHub repo, Google Drive link,
     * Figma file, video, deployed app, etc.
     */
    val submittedUrl: String = "",

    /**
     * Optional handover note from the worker explaining the submission.
     * E.g., "Check the /docs folder for setup instructions."
     */
    val submittedNote: String = "",

    /**
     * ISO timestamp of when the worker submitted.
     * Used by the backend scheduler to auto-complete after 3 days.
     */
    @SerializedName("submittedAt")
    val submittedAt: String = ""
)
