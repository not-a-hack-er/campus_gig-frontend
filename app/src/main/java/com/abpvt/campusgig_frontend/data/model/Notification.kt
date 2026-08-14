package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("_id")
    val id: String = "",
    val recipient: String = "",
    val type: String = "",           // "application", "message", "review", etc.
    val title: String = "",
    val body: String = "",
    val isRead: Boolean = false,
    val referenceId: String = "",    // e.g. gigId, applicationId
    val referenceType: String = "",  // "Gig" | "Application" | "Message"
    @SerializedName("createdAt")
    val createdAt: String = ""
)
