package com.abpvt.campusgig_frontend

import android.app.Application
import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.network.RetrofitInstance
import com.abpvt.campusgig_frontend.core.utils.Constants
import com.abpvt.campusgig_frontend.data.repository.AuthRepository
import com.abpvt.campusgig_frontend.data.repository.ChatRepository
import com.abpvt.campusgig_frontend.data.repository.CommunityRepository
import com.abpvt.campusgig_frontend.data.repository.GigRepository
import com.abpvt.campusgig_frontend.data.repository.UserRepository
import com.abpvt.campusgig_frontend.features.chat.SocketManager

/**
 * Application class — acts as a simple manual DI container.
 * Access repositories via [CampusGigApplication.instance].
 * Replace with Hilt/Koin for production-scale DI.
 */
class CampusGigApplication : Application() {

    companion object {
        lateinit var instance: CampusGigApplication
            private set
    }

    // ── Network ──────────────────────────────────────────────────────────────
    val apiService: ApiService by lazy { RetrofitInstance.create(this) }
    val api: ApiService get() = apiService // Shorthand alias used by newer ViewModels

    // ── Repositories ─────────────────────────────────────────────────────────
    val authRepository: AuthRepository by lazy { AuthRepository(apiService, this) }
    val gigRepository: GigRepository by lazy { GigRepository(apiService) }
    val chatRepository: ChatRepository by lazy { ChatRepository(apiService) }
    val communityRepository: CommunityRepository by lazy { CommunityRepository(apiService) }
    val userRepository: UserRepository by lazy { UserRepository(apiService) }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Connect socket if already logged in
        val token = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
            .getString(Constants.KEY_TOKEN, null)
        if (token != null) SocketManager.connect(token)
    }
}
