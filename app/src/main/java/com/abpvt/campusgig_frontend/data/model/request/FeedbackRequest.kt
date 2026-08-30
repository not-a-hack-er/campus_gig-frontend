package com.abpvt.campusgig_frontend.data.model.request

import com.google.gson.annotations.SerializedName

/**
 * FeedbackRequest.kt — Request body DTO for POST /api/feedback
 */
data class FeedbackRequest(
    val type: String = "GENERAL_HELP",
    val rating: Int? = null,
    val message: String,
    val deviceInfo: String = "Android App v3.0"
)
