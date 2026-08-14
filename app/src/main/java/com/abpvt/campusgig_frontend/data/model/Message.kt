package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("_id")
    val id: String = "",
    val sender: User? = null,
    val receiver: User? = null,
    val content: String = "",
    val isRead: Boolean = false,
    @SerializedName("createdAt")
    val createdAt: String = ""
)
