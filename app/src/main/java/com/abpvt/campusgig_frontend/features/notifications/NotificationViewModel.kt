package com.abpvt.campusgig_frontend.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val api: ApiService) : ViewModel() {

    private val _notifications = MutableStateFlow<Resource<List<Notification>>>(Resource.Loading)
    val notifications: StateFlow<Resource<List<Notification>>> = _notifications

    init {
        loadNotifications()
        com.abpvt.campusgig_frontend.features.chat.SocketManager.onNewNotification {
            loadNotifications()
        }
    }

    override fun onCleared() {
        super.onCleared()
        com.abpvt.campusgig_frontend.features.chat.SocketManager.removeNotificationListener()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _notifications.value = Resource.Loading
            try {
                val response = api.getNotifications()
                _notifications.value = if (response.isSuccessful)
                    Resource.Success(response.body()!!)
                else
                    Resource.Error(response.message(), response.code())
            } catch (e: Exception) {
                _notifications.value = Resource.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                api.markNotificationRead(id)
                // Refresh after marking read
                loadNotifications()
            } catch (_: Exception) { }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                api.markAllNotificationsRead()
                loadNotifications()
            } catch (_: Exception) { }
        }
    }
}
