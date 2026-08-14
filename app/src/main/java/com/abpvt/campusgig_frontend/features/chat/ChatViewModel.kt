/**
 * ChatViewModel.kt — Manages chat state combining REST history + Socket.IO real-time (v3.0)
 */
package com.abpvt.campusgig_frontend.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.ConversationItem
import com.abpvt.campusgig_frontend.data.model.Message
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    // ── Conversation List (used in ChatListScreen) ─────────────────────────────
    private val _conversations = MutableStateFlow<Resource<List<ConversationItem>>>(Resource.Loading)
    val conversations: StateFlow<Resource<List<ConversationItem>>> = _conversations.asStateFlow()

    // ── Global Online Users Set (userIDs currently connected) ──────────────────
    private val _onlineUserIds = MutableStateFlow<Set<String>>(emptySet())
    val onlineUserIds: StateFlow<Set<String>> = _onlineUserIds.asStateFlow()

    // ── Message History (used in ChatScreen) ──────────────────────────────────
    private val _messages = MutableStateFlow<Resource<List<Message>>>(Resource.Loading)
    val messages: StateFlow<Resource<List<Message>>> = _messages.asStateFlow()

    // ── Typing & Presence Indicator State ─────────────────────────────────────
    private val _isPeerTyping = MutableStateFlow(false)
    val isPeerTyping: StateFlow<Boolean> = _isPeerTyping.asStateFlow()

    private val _isPeerOnline = MutableStateFlow(false)
    val isPeerOnline: StateFlow<Boolean> = _isPeerOnline.asStateFlow()

    private var lastTypingTime = 0L

    // Track current conversation receiver
    private var currentReceiverId = ""
    private var currentUserId     = ""

    init {
        loadConversations()
        listenForGlobalSocketUpdates()
    }

    /**
     * Listens for global online user lists and conversation updates across the whole app.
     */
    private fun listenForGlobalSocketUpdates() {
        SocketManager.onOnlineUsers { list ->
            val set = list.toSet()
            _onlineUserIds.value = set
            if (currentReceiverId.isNotBlank()) {
                _isPeerOnline.value = set.contains(currentReceiverId)
            }
        }

        SocketManager.onConversationUpdated { obj ->
            // Reload inbox whenever any conversation updates
            loadConversations()
        }
    }

    /**
     * Loads the inbox — the list of conversations the current user is part of.
     */
    fun loadConversations() {
        viewModelScope.launch {
            val res = repository.getInbox()
            _conversations.value = res
        }
    }

    /**
     * Loads chat history with a specific user AND registers socket listeners
     * for real-time incoming messages, presence, and read receipts.
     */
    fun loadMessages(receiverId: String, myUserId: String = "") {
        currentReceiverId = receiverId
        currentUserId     = myUserId

        viewModelScope.launch {
            _messages.value = Resource.Loading
            _messages.value = repository.getMessages(receiverId)

            // Mark unread messages as read in DB and emit read event to peer
            repository.markMessagesRead(receiverId)
            SocketManager.emitMarkRead(receiverId)
        }

        // Check current cached online users
        _isPeerOnline.value = _onlineUserIds.value.contains(receiverId)
        SocketManager.requestOnlineUsers()

        // Register socket listener for new messages in this conversation.
        SocketManager.onNewMessage { messageJson ->
            appendIncomingMessage(messageJson)

            // If we are currently viewing this chat and receive a new message from peer, mark read
            val senderObj = messageJson.optJSONObject("sender")
            val senderId  = senderObj?.optString("_id", "") ?: messageJson.optString("sender", "")
            if (senderId == receiverId) {
                viewModelScope.launch {
                    repository.markMessagesRead(receiverId)
                    SocketManager.emitMarkRead(receiverId)
                }
            }
        }

        // Register socket listener for live read receipt confirmation
        SocketManager.onMessagesRead { readerId ->
            if (readerId == receiverId) {
                val currentList = (_messages.value as? Resource.Success)?.data?.map { msg ->
                    if (msg.sender?.id == myUserId || msg.sender?.id.isNullOrBlank()) {
                        msg.copy(isRead = true)
                    } else {
                        msg
                    }
                }
                if (currentList != null) {
                    _messages.value = Resource.Success(currentList)
                }
            }
        }

        // Register socket listener for typing indicator
        SocketManager.onTyping { senderId ->
            if (senderId == receiverId) {
                _isPeerTyping.value = true
                viewModelScope.launch {
                    kotlinx.coroutines.delay(3000)
                    _isPeerTyping.value = false
                }
            }
        }
    }

    /**
     * Sends a typing event via Socket.IO, throttled to once every 2 seconds.
     */
    fun sendTypingEvent(receiverId: String) {
        val now = System.currentTimeMillis()
        if (now - lastTypingTime > 2000) {
            lastTypingTime = now
            SocketManager.emitTyping(receiverId)
        }
    }

    /**
     * OPTIMISTIC SEND:
     * Adds message locally instantly, then emits to Socket.IO.
     */
    fun sendMessage(receiverId: String, content: String, myUser: User? = null) {
        val tempId = "temp_${System.currentTimeMillis()}"
        val optimisticMessage = Message(
            id        = tempId,
            sender    = myUser ?: User(id = currentUserId),
            receiver  = User(id = receiverId),
            content   = content,
            isRead    = false,
            createdAt = java.time.Instant.now().toString()
        )
        val currentList = (_messages.value as? Resource.Success)?.data?.toMutableList()
            ?: mutableListOf()
        currentList.add(optimisticMessage)
        _messages.value = Resource.Success(currentList.toList())

        // Emit to socket — server saves, broadcasts back
        SocketManager.sendMessage(receiverId, content)

        // Reload inbox list in background so the new conversation immediately appears in Inbox
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            loadConversations()
        }
    }

    /**
     * Appends a new message received from Socket.IO to the existing list.
     */
    private fun appendIncomingMessage(json: JSONObject) {
        val currentList = (_messages.value as? Resource.Success)?.data?.toMutableList()
            ?: mutableListOf()

        val senderObj = json.optJSONObject("sender")
        val senderId  = senderObj?.optString("_id", "") ?: json.optString("sender", "")
        val sender = if (senderObj != null) {
            User(
                id             = senderId,
                name           = senderObj.optString("name", ""),
                profilePicture = senderObj.optString("avatar", ""),
                college        = senderObj.optString("college", "")
            )
        } else {
            if (senderId.isNotBlank()) User(id = senderId) else null
        }

        val receiverObj  = json.optJSONObject("receiver")
        val receiverId   = receiverObj?.optString("_id", "") ?: json.optString("receiver", "")
        val receiver     = if (receiverId.isNotBlank()) User(id = receiverId) else null

        val incomingContent = json.optString("content", "")
        val incomingId      = json.optString("_id", "srv_${System.currentTimeMillis()}")

        // Dedup: check if we have a matching optimistic "temp_" message.
        val tempIndex = currentList.indexOfFirst { msg ->
            msg.id.startsWith("temp_") &&
            msg.content == incomingContent &&
            (msg.sender?.id == senderId || msg.sender?.id == currentUserId)
        }

        val newMessage = Message(
            id        = incomingId,
            sender    = sender,
            receiver  = receiver,
            content   = incomingContent,
            isRead    = json.optBoolean("isRead", false),
            createdAt = json.optString("createdAt", "")
        )

        if (tempIndex >= 0) {
            currentList[tempIndex] = newMessage
        } else {
            currentList.add(newMessage)
        }

        _messages.value = Resource.Success(currentList.toList())
    }

    /**
     * Removes chat-specific socket listeners when leaving the chat screen.
     */
    fun cleanup() {
        SocketManager.removeChatListeners()
        currentReceiverId = ""
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.removeListeners()
    }
}
