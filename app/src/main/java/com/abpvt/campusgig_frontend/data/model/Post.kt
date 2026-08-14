package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

/**
 * Post.kt — Data model for discussion threads posted inside CampusGig Communities.
 *
 * Posts allow students to share ideas, ask questions, or link resources.
 * Each post belongs to a parent Community and has a User author.
 */
data class Post(
    @SerializedName("_id")
    val id: String = "",

    val communityId: String = "",      // The ID of the community this post belongs to
    val author: User? = null,          // Author of the post (populated by backend)
    val content: String = "",          // The actual message / text content
    val likesCount: Int = 0,           // Total likes count
    val likedBy: List<String> = emptyList(), // List of user IDs who liked the post

    @SerializedName("createdAt")
    val createdAt: String = ""
)
