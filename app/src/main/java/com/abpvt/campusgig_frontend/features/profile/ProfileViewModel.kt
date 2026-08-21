/**
 * ProfileViewModel.kt — Manages profile data fetch, update, and logout.
 *
 * OPERATIONS:
 * 1. loadProfile()   — Fetch current user's profile (GET /users/me)
 * 2. updateProfile() — Save edits (PUT /users/me)
 * 3. logout()        — Clear stored token via AuthRepository
 */
package com.abpvt.campusgig_frontend.features.profile
import androidx.compose.material3.MaterialTheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.CampusGigApplication
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    // Current user's profile data
    private val _profile = MutableStateFlow<Resource<User>>(Resource.Loading)
    val profile: StateFlow<Resource<User>> = _profile.asStateFlow()

    // State for profile update operation (null = idle)
    private val _updateState = MutableStateFlow<Resource<User>?>(null)
    val updateState: StateFlow<Resource<User>?> = _updateState.asStateFlow()

    init {
        loadProfile()
    }

    /** Fetches the logged-in user's own profile from the backend. */
    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = Resource.Loading
            _profile.value = repository.getMyProfile()
        }
    }

    /**
     * Sends updated profile data to the server.
     * On success, also updates the local profile state.
     *
     * @param updatedUser A [User] object with the new values filled in.
     */
    fun updateProfile(updatedUser: User) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val result = repository.updateMyProfile(updatedUser)
            _updateState.value = result
            // If update succeeded, refresh the local profile display
            if (result is Resource.Success<User>) {
                _profile.value = result
            }
        }
    }

    /**
     * Logs the user out by clearing the stored JWT token.
     * The UI should then navigate to the LoginScreen and clear the back stack.
     */
    fun logout() {
        // Access Application-level AuthRepository for logout
        // (ProfileViewModel only knows UserRepository, so we need the app context)
        CampusGigApplication.instance.authRepository.logout()
    }

    /** Reset update state after navigation. */
    fun resetUpdateState() { _updateState.value = null }
}
