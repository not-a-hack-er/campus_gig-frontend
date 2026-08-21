package com.abpvt.campusgig_frontend.data.repository

import android.content.Context
import com.abpvt.campusgig_frontend.core.network.ApiService
import com.abpvt.campusgig_frontend.core.utils.Constants
import com.abpvt.campusgig_frontend.core.utils.Resource
import com.abpvt.campusgig_frontend.core.utils.toResourceError
import com.abpvt.campusgig_frontend.data.model.User
import com.abpvt.campusgig_frontend.data.model.request.LoginRequest
import com.abpvt.campusgig_frontend.data.model.request.RegisterRequest
import com.abpvt.campusgig_frontend.data.model.response.AuthResponse

class AuthRepository(
    private val api: ApiService,
    private val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                // Safe unwrap: return descriptive error if body is unexpectedly null
                val body = response.body()
                    ?: return Resource.Error("Server returned an empty response. Please try again.", response.code())
                saveSession(body.token, body.user)
                Resource.Success(body)
            } else {
                // Parse backend's custom JSON error message (e.g. "Invalid credentials")
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun googleLogin(name: String, email: String): Resource<AuthResponse> {
        return try {
            val response = api.googleLogin(mapOf("name" to name, "email" to email))
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Resource.Error("Server returned an empty response. Please try again.", response.code())
                saveSession(body.token, body.user)
                Resource.Success(body)
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    /**
     * Registers a new user.
     *
     * [branch], [yearOfStudy], and [skills] are now properly forwarded to the backend.
     * Previously these were collected in Step 2 of the UI but silently dropped here.
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        college: String,
        branch: String = "",
        yearOfStudy: String = "",
        skills: List<String> = emptyList()
    ): Resource<AuthResponse> {
        return try {
            val response = api.register(
                RegisterRequest(
                    name        = name,
                    email       = email,
                    password    = password,
                    role        = role,
                    college     = college,
                    branch      = branch,
                    yearOfStudy = yearOfStudy,
                    skills      = skills
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                    ?: return Resource.Error("Server returned an empty response. Please try again.", response.code())
                saveSession(body.token, body.user)
                Resource.Success(body)
            } else {
                // Parse backend's custom JSON error (e.g. "Email already registered")
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun forgotPassword(email: String): Resource<String> {
        return try {
            val response = api.forgotPassword(mapOf("email" to email.trim()))
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "OTP sent successfully"
                Resource.Success(message)
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun verifyOtp(email: String, otp: String): Resource<String> {
        return try {
            val response = api.verifyOtp(mapOf("email" to email.trim(), "otp" to otp.trim()))
            if (response.isSuccessful) {
                val resetToken = response.body()?.data?.resetToken
                if (!resetToken.isNullOrBlank()) {
                    Resource.Success(resetToken)
                } else {
                    Resource.Error("Invalid OTP response from server")
                }
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    suspend fun resetPassword(resetToken: String, newPassword: String): Resource<String> {
        return try {
            val response = api.resetPassword(mapOf("resetToken" to resetToken, "newPassword" to newPassword))
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Password reset successfully"
                Resource.Success(message)
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getString(Constants.KEY_TOKEN, null) != null

    fun getSavedToken(): String? = prefs.getString(Constants.KEY_TOKEN, null)

    private fun saveSession(token: String, user: User) {
        prefs.edit()
            .putString(Constants.KEY_TOKEN, token)
            .putString(Constants.KEY_USER_ID, user.id)
            .putString(Constants.KEY_USER_ROLE, user.role)
            .putString(Constants.KEY_USER_NAME, user.name)
            .putString(Constants.KEY_USER_EMAIL, user.email)
            .apply()
    }
}
