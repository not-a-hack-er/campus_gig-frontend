/**
 * SocketManager.kt — Singleton Manager for Socket.IO Real-time Messaging & Presence (v3.0)
 */
package com.abpvt.campusgig_frontend.features.chat

import android.util.Log
import com.abpvt.campusgig_frontend.core.utils.Constants
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {

    private const val TAG = "SocketManager"

    /** The active Socket.IO connection. Null when disconnected. */
    private var socket: Socket? = null

    /** Cached token for auto-reconnect */
    private var savedToken: String? = null

    /**
     * Establishes the Socket.IO WebSocket connection with JWT token.
     */
    fun connect(token: String) {
        savedToken = token

        if (socket != null && socket?.connected() == true) {
            Log.d(TAG, "Socket already connected, requesting status updates")
            requestOnlineUsers()
            return
        }

        if (socket != null) {
            socket?.disconnect()
            socket?.off()
            socket = null
        }

        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 1000
            }

            socket = IO.socket(Constants.SOCKET_URL, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Socket connected successfully")
                socket?.emit("user_online")
                socket?.emit("get_online_users")
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                Log.d(TAG, "❌ Socket disconnected: ${args.firstOrNull()}")
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "🔴 Socket connection error: ${args.firstOrNull()}")
            }

            socket?.connect()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create socket connection: ${e.message}")
        }
    }

    /**
     * Explicitly requests the latest online users list and re-announces presence.
     */
    fun requestOnlineUsers() {
        socket?.emit("user_online")
        socket?.emit("get_online_users")
    }

    /**
     * Registers a callback for online user updates ("online_users" event).
     */
    fun onOnlineUsers(callback: (List<String>) -> Unit) {
        socket?.off("online_users")
        socket?.on("online_users") { args ->
            val jsonArray = args.getOrNull(0) as? org.json.JSONArray ?: return@on
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.optString(i))
            }
            callback(list)
        }
        // Immediately request current online users upon registering callback
        requestOnlineUsers()
    }

    /**
     * Registers a callback for real-time conversation/inbox updates ("conversation_updated").
     */
    fun onConversationUpdated(callback: (JSONObject) -> Unit) {
        socket?.off("conversation_updated")
        socket?.on("conversation_updated") { args ->
            val data = args.getOrNull(0) as? JSONObject
            if (data != null) {
                callback(data)
            }
        }
    }

    /**
     * Emits a read receipt notification to the sender.
     */
    fun emitMarkRead(senderId: String) {
        socket?.emit("mark_read", senderId)
    }

    /**
     * Registers a callback for when messages sent by current user have been read by [readerId].
     */
    fun onMessagesRead(callback: (String) -> Unit) {
        socket?.off("messages_read")
        socket?.on("messages_read") { args ->
            val obj = args.getOrNull(0) as? JSONObject
            val readerId = obj?.optString("readerId", "") ?: ""
            if (readerId.isNotBlank()) {
                callback(readerId)
            }
        }
    }

    /**
     * Disconnects the WebSocket connection manually.
     */
    fun disconnect() {
        socket?.disconnect()
        socket = null
        savedToken = null
        Log.d(TAG, "Socket disconnected manually")
    }

    /**
     * Joins a private chat "room" between two users.
     */
    fun joinRoom(roomId: String) {
        socket?.emit(Constants.SOCKET_JOIN_ROOM, roomId)
        Log.d(TAG, "Joined room: $roomId")
    }

    /**
     * Emits a chat message to the server. Auto-reconnects if socket is disconnected.
     */
    fun sendMessage(receiverId: String, content: String) {
        if (socket?.connected() != true && !savedToken.isNullOrBlank()) {
            Log.w(TAG, "Socket disconnected — reconnecting before send")
            connect(savedToken!!)
        }

        val data = JSONObject().apply {
            put("receiverId", receiverId)
            put("content", content)
        }
        socket?.emit(Constants.SOCKET_SEND_MESSAGE, data)
        Log.d(TAG, "Message sent to $receiverId: $content")
    }

    /**
     * Registers a callback for incoming messages in a specific chat.
     */
    fun onNewMessage(callback: (JSONObject) -> Unit) {
        socket?.off(Constants.SOCKET_NEW_MESSAGE)
        socket?.on(Constants.SOCKET_NEW_MESSAGE) { args ->
            val message = args.getOrNull(0) as? JSONObject
            if (message != null) {
                Log.d(TAG, "New message received: ${message.optString("content")}")
                callback(message)
            }
        }
    }

    /**
     * Emits a "typing" event to notify the other user we're typing.
     */
    fun emitTyping(receiverId: String) {
        socket?.emit(Constants.SOCKET_TYPING, receiverId)
    }

    /**
     * Registers a callback for the "typing" indicator from the other user.
     */
    fun onTyping(callback: (String) -> Unit) {
        socket?.off(Constants.SOCKET_TYPING)
        socket?.on(Constants.SOCKET_TYPING) { args ->
            val senderId = args.getOrNull(0) as? String ?: return@on
            callback(senderId)
        }
    }

    /**
     * Registers a callback for incoming notifications.
     */
    fun onNewNotification(callback: (JSONObject) -> Unit) {
        socket?.off("new_notification")
        socket?.on("new_notification") { args ->
            val notification = args.getOrNull(0) as? JSONObject
            if (notification != null) {
                Log.d(TAG, "New notification received: ${notification.optString("title")}")
                callback(notification)
            }
        }
    }

    fun removeNotificationListener() {
        socket?.off("new_notification")
    }

    /**
     * Removes chat-specific event listeners when leaving the chat screen.
     */
    fun removeChatListeners() {
        socket?.off(Constants.SOCKET_NEW_MESSAGE)
        socket?.off(Constants.SOCKET_TYPING)
        socket?.off("messages_read")
    }

    fun removeListeners() {
        removeChatListeners()
        socket?.off("online_users")
        socket?.off("conversation_updated")
    }

    /** Returns true if the socket is currently connected. */
    fun isConnected(): Boolean = socket?.connected() == true
}
