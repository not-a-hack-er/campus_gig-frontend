/**
 * AuthViewModel.kt — Manages authentication state for Login and Register screens.
 *
 * RESPONSIBILITIES:
 * - Triggers login/register API calls through [AuthRepository]
 * - Exposes state as [StateFlow] so the UI can react to changes
 * - Persists the JWT token and user info on success (handled in repository)
 * - Provides logout capability
 *
 * STATE FLOW:
 *   null → Loading → Success(AuthResponse) or Error("message")
 *
 * The UI observes [authState] and:
 * - Shows a spinner on Loading
 * - Navigates to Home on Success
 * - Shows an error message on Error
 */
package com.abpvt.campusgig_frontend.features.auth
import androidx.compose.material3.MaterialTheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.data.model.response.AuthResponse
import com.abpvt.campusgig_frontend.data.repository.AuthRepository
import com.abpvt.campusgig_frontend.features.chat.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val authState: StateFlow<Resource<AuthResponse>?> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading  // Tell UI to show spinner
            val result = repository.login(email.trim(), password)
            if (result is Resource.Success) {
                SocketManager.connect(result.data.token)
            }
            _authState.value = result
        }
    }

    fun googleLogin(name: String, email: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            val result = repository.googleLogin(name, email)
            if (result is Resource.Success) {
                SocketManager.connect(result.data.token)
            }
            _authState.value = result
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        college: String,
        branch: String = "",
        yearOfStudy: String = "",
        skills: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            val result = repository.register(
                name        = name.trim(),
                email       = email.trim(),
                password    = password,
                role        = role,
                college     = college.trim(),
                branch      = branch.trim(),
                yearOfStudy = yearOfStudy.trim(),
                skills      = skills
            )
            if (result is Resource.Success) {
                SocketManager.connect(result.data.token)
            }
            _authState.value = result
        }
    }

    /** Clears token from SharedPreferences, effectively logging the user out. */
    fun logout() {
        SocketManager.disconnect()
        repository.logout()
    }

    fun resetState() {
        _authState.value = null
    }
}
