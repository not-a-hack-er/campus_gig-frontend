package com.abpvt.campusgig_frontend.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * CompletionOtpResponse.kt — Response model when the gig owner requests/refreshes completion OTP.
 * Backend endpoint: GET /api/gigs/:id/completion-otp
 */
data class CompletionOtpResponse(
    @SerializedName("otp")
    val otp: String = "",

    @SerializedName("expiresAt")
    val expiresAt: String = ""
)
