/**
 * ViewModelFactory.kt — ViewModel factory implementations for CampusGig.
 *
 * WHY DO WE NEED THIS?
 * By default, Jetpack's `viewModel()` composable can only create ViewModels
 * that have a no-argument constructor. Since our ViewModels require dependencies
 * (repositories, ApiService), we must use a ViewModelProvider.Factory to tell
 * Android HOW to create each ViewModel.
 *
 * In production apps, Hilt or Koin would handle this automatically.
 * Here we use a manual factory pattern for simplicity and full transparency.
 *
 * HOW TO USE:
 * In a Composable, instead of:
 *   val vm: AuthViewModel = viewModel()   // ❌ Won't work — has constructor args
 *
 * Use:
 *   val app = LocalContext.current.applicationContext as CampusGigApplication
 *   val vm: AuthViewModel = viewModel(factory = AuthViewModelFactory(app.authRepository))
 *
 * Each screen has its own companion factory at the bottom of this file.
 */
package com.abpvt.campusgig_frontend.core.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abpvt.campusgig_frontend.data.repository.AuthRepository
import com.abpvt.campusgig_frontend.data.repository.ChatRepository
import com.abpvt.campusgig_frontend.data.repository.CommunityRepository
import com.abpvt.campusgig_frontend.data.repository.GigRepository
import com.abpvt.campusgig_frontend.data.repository.UserRepository
import com.abpvt.campusgig_frontend.features.auth.AuthViewModel
import com.abpvt.campusgig_frontend.features.auth.ForgotPasswordViewModel
import com.abpvt.campusgig_frontend.features.chat.ChatViewModel
import com.abpvt.campusgig_frontend.features.communities.CommunityViewModel
import com.abpvt.campusgig_frontend.features.gigs.GigViewModel
import com.abpvt.campusgig_frontend.features.home.HomeViewModel
import com.abpvt.campusgig_frontend.features.notifications.NotificationViewModel
import com.abpvt.campusgig_frontend.features.profile.ProfileViewModel
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.features.applications.ApplicationViewModel

// ─── Auth ViewModel Factory ───────────────────────────────────────────────────
/**
 * Creates [AuthViewModel] with the [AuthRepository] dependency injected.
 */
class AuthViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

// ─── Forgot Password ViewModel Factory ────────────────────────────────────────
class ForgotPasswordViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            return ForgotPasswordViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

// ─── Home ViewModel Factory ───────────────────────────────────────────────────
class HomeViewModelFactory(
    private val gigRepo: GigRepository,
    private val userRepo: UserRepository,
    private val api: ApiService = CampusGigApplication.instance.apiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(gigRepo, userRepo, api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

// ─── Gig ViewModel Factory ────────────────────────────────────────────────────
class GigViewModelFactory(private val repo: GigRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GigViewModel::class.java)) {
            return GigViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

// ─── Application ViewModel Factory ───────────────────────────────────────────
class ApplicationViewModelFactory(private val api: ApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApplicationViewModel::class.java)) {
            return ApplicationViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

// ─── Chat ViewModel Factory ───────────────────────────────────────────────────
class ChatViewModelFactory(private val repo: ChatRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

// ─── Community ViewModel Factory ──────────────────────────────────────────────
class CommunityViewModelFactory(private val repo: CommunityRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommunityViewModel::class.java)) {
            return CommunityViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

// ─── Notification ViewModel Factory ──────────────────────────────────────────
class NotificationViewModelFactory(private val api: ApiService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

// ─── Profile ViewModel Factory ────────────────────────────────────────────────
class ProfileViewModelFactory(private val repo: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
