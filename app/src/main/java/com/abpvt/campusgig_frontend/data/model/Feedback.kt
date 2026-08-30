package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

/**
 * Feedback.kt — Data model for user submitted feedback/support request.
 * Endpoint: GET /api/feedback/my
 */
data class Feedback(
    @SerializedName("_id")
    val id: String = "",

    val type: String = "GENERAL_HELP",

    val rating: Int? = null,

    val message: String = "",

    val deviceInfo: String = "",

    val status: String = "PENDING",

    @SerializedName("createdAt")
    val createdAt: String = ""
)
