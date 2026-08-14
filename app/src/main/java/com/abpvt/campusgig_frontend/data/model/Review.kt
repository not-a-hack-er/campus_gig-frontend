package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("_id")
    val id: String = "",
    val reviewer: User? = null,
    val reviewee: User? = null,
    val gig: Gig? = null,
    val rating: Int = 0,             // 1–5
    val comment: String = "",
    @SerializedName("createdAt")
    val createdAt: String = ""
)
