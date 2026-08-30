package com.abpvt.campusgig_frontend.data.repository

import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.ConversationItem
import com.abpvt.campusgig_frontend.data.model.Message

class ChatRepository(private val api: ApiService) {

    /**
     * Fetches all messages exchanged with a specific user.
     *
     * The backend finds (or returns empty for) the conversation between
     * [req.user.id] and [receiverId], then returns all Message documents
     * sorted oldest-first, with sender populated ({ _id, name, avatar }).
     */
    suspend fun getMessages(receiverId: String, gigId: String): Resource<List<Message>> {
        return try {
            val response = api.getMessages(receiverId, gigId)
            if (response.isSuccessful) Resource.Success(response.body() ?: emptyList())
            else Resource.Error(response.message(), response.code())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    /**
     * Fetches the inbox: all conversations this user is part of.
     *
     * Each item contains { conversationId, lastMessage, updatedAt, user }
     * where [user] is the OTHER participant (not the logged-in user).
     * Sorted by most recently active first.
     *
     * Endpoint: GET /api/messages/inbox
     * (was /conversations — renamed to avoid Express wildcard collision)
     */
    suspend fun getInbox(): Resource<List<ConversationItem>> {
        return try {
            val response = api.getInbox()
            if (response.isSuccessful) Resource.Success(response.body() ?: emptyList())
            else Resource.Error(response.message(), response.code())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    suspend fun markMessagesRead(receiverId: String, gigId: String): Resource<Boolean> {
        return try {
            val response = api.markMessagesRead(receiverId, gigId)
            if (response.isSuccessful) Resource.Success(true)
            else Resource.Error(response.message(), response.code())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }
}
