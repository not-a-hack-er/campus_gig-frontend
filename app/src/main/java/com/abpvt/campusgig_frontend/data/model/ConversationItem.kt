package com.abpvt.campusgig_frontend.data.model

import com.google.gson.annotations.SerializedName

/**
 * ConversationItem.kt — Represents one row in the inbox (chat list).
 *
 * This is the shape returned by GET /api/messages/inbox:
 * {
 *   "conversationId": "abc123",
 *   "lastMessage": "Sure, let me check!",
 *   "updatedAt": "2026-07-13T00:00:00.000Z",
 *   "user": { "_id": "...", "name": "...", "avatar": "...", ... }
 * }
 *
 * WHY A SEPARATE MODEL (not just User)?
 * The inbox shows more than just who you talked to — it needs the last
 * message preview and the timestamp so users can see which chat is newest.
 * We can't fit that into the User model without polluting it with chat data.
 */
data class ConversationItem(
    @SerializedName("conversationId")
    val conversationId: String = "",

    /** Preview of the most recent message in this conversation */
    @SerializedName("lastMessage")
    val lastMessage: String = "",

    /** ISO 8601 timestamp of last activity — used for sorting and display */
    @SerializedName("updatedAt")
    val updatedAt: String = "",

    /** The other participant in this conversation (not the logged-in user) */
    @SerializedName("user")
    val user: User
)
