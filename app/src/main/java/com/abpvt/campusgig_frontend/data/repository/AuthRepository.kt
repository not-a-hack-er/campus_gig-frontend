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
import com.abpvt.campusgig_frontend.BuildConfig
import com.clerk.api.Clerk
import com.clerk.api.auth.types.IdTokenProvider
import com.clerk.api.network.serialization.ClerkResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class AuthRepository(
    private val api: ApiService,
    private val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun parseAuthData(rawJson: String): AuthResponse? {
        if (rawJson.isBlank()) return null
        return try {
            val gson = com.google.gson.Gson()
            val jsonObj = org.json.JSONObject(rawJson)

            if (jsonObj.has("data") && !jsonObj.isNull("data")) {
                val dataObj = jsonObj.getJSONObject("data")
                gson.fromJson(dataObj.toString(), AuthResponse::class.java)
            } else if (jsonObj.has("token") && jsonObj.has("user")) {
                gson.fromJson(rawJson, AuthResponse::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val rawJson = response.body()?.string() ?: ""
                val authData = parseAuthData(rawJson)
                if (authData != null && !authData.token.isNullOrBlank()) {
                    saveSession(authData.token, authData.user)
                    Resource.Success(authData)
                } else {
                    Resource.Error("Server returned invalid session data.")
                }
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
                val rawJson = response.body()?.string() ?: ""
                val authData = parseAuthData(rawJson)
                if (authData != null && !authData.token.isNullOrBlank()) {
                    saveSession(authData.token, authData.user)
                    Resource.Success(authData)
                } else {
                    Resource.Error("Server returned invalid session data.")
                }
            } else {
                response.toResourceError()
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error — please check your connection")
        }
    }

    /**
     * Google identity is established by Clerk, then /auth/sync safely links the
     * verified Clerk email to its existing CampusVault profile (or creates one).
     */
    suspend fun loginWithGoogle(idToken: String): Resource<AuthResponse> {
        return try {
            // ── 1. Try Direct Backend Google OAuth Verification ────────────────
            val backendResponse = api.googleLogin(mapOf("idToken" to idToken))
            android.util.Log.i("CampusAuth", "stage=backend_google_login http=${backendResponse.code()}")
            if (backendResponse.isSuccessful) {
                val rawJson = backendResponse.body()?.string() ?: ""
                val authData = parseAuthData(rawJson)
                if (authData != null && !authData.token.isNullOrBlank()) {
                    saveSession(authData.token, authData.user, provider = "legacy")
                    return Resource.Success(authData)
                }
            }

            // ── 2. Fallback to Clerk ──────────────────────────────────────────
            if (withTimeoutOrNull(5_000) { Clerk.isInitialized.first { it } } != true) {
                try {
                    Clerk.reinitialize()
                } catch (e: Exception) {
                    android.util.Log.w("CampusAuth", "Clerk reinitialize attempt failed: ${e.message}")
                }
                if (withTimeoutOrNull(10_000) { Clerk.isInitialized.first { it } } != true) {
                    return Resource.Error("Authentication service did not initialize. Please check your internet connection and try again.")
                }
            }

            val signIn = Clerk.auth.signInWithIdToken {
                token = idToken
                provider = IdTokenProvider.GOOGLE
            }
            if (signIn is ClerkResult.Failure) {
                val err = signIn.error?.errors?.firstOrNull()
                android.util.Log.e("CampusAuth", "signInWithIdToken failed: code=${err?.code} msg=${err?.message} longMsg=${err?.longMessage}")
            }
            val signInValue = (signIn as? ClerkResult.Success)?.value
            var sessionId = signInValue?.signIn?.createdSessionId ?: signInValue?.signUp?.createdSessionId

            if (sessionId.isNullOrBlank()) {
                val signUp = Clerk.auth.signUpWithIdToken(idToken, IdTokenProvider.GOOGLE)
                if (signUp is ClerkResult.Failure) {
                    val err = signUp.error?.errors?.firstOrNull()
                    android.util.Log.e("CampusAuth", "signUpWithIdToken failed: code=${err?.code} msg=${err?.message} longMsg=${err?.longMessage}")
                }
                val signUpSuccess = (signUp as? ClerkResult.Success)?.value
                sessionId = signUpSuccess?.createdSessionId
                if (sessionId.isNullOrBlank() && signUp is ClerkResult.Failure) {
                    val err = signUp.error?.errors?.firstOrNull()
                    val code = err?.code
                    val msg = err?.longMessage ?: err?.message
                    android.util.Log.w("CampusAuth", "stage=clerk_signup http=${signUp.code} code=$code msg=$msg")
                    return Resource.Error("Google authentication failed. Please try again.")
                }
            }

            if (sessionId.isNullOrBlank()) {
                return Resource.Error("Google account could not be authenticated with Clerk. Please try again.")
            }

            val activation = Clerk.auth.setActive(sessionId)
            if (activation !is ClerkResult.Success) {
                return Resource.Error("Google sign-in completed, but the Clerk session could not be activated.")
            }
            val token = when (val tokenResult = Clerk.auth.getToken()) {
                is ClerkResult.Success -> tokenResult.value
                is ClerkResult.Failure -> null
            }
            if (token.isNullOrBlank()) {
                return Resource.Error("Clerk did not provide a session token. Please try again.")
            }

            val response = api.syncClerkUser("Bearer $token")
            android.util.Log.i("CampusAuth", "stage=profile_sync http=${response.code()}")
            if (response.isSuccessful) {
                val rawJson = response.body()?.string() ?: ""
                val authData = parseClerkSync(rawJson, token)
                if (authData != null) {
                    saveSession(authData.token, authData.user, provider = "clerk")
                    Resource.Success(authData)
                } else {
                    Resource.Error("Your Clerk account was created, but the CampusVault profile could not be synced.")
                }
            } else {
                response.toResourceError()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.w("CampusAuth", "stage=clerk_exception type=${e.javaClass.simpleName}")
            Resource.Error("Google sign-in could not be completed. Please try again.")
        }
    }

    suspend fun logout() {
        if (prefs.getString(Constants.KEY_AUTH_PROVIDER, "legacy") == "clerk") {
            try { Clerk.auth.signOut() } catch (_: Exception) { /* Local cleanup still proceeds. */ }
        }
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getString(Constants.KEY_TOKEN, null) != null

    fun getSavedToken(): String? = prefs.getString(Constants.KEY_TOKEN, null)

    private fun parseClerkSync(rawJson: String, token: String): AuthResponse? {
        return try {
            val data = org.json.JSONObject(rawJson).optJSONObject("data") ?: return null
            if (data.optString("userId").isBlank()) return null
            AuthResponse(
                token = token,
                user = User(
                    id = data.optString("userId"),
                    name = data.optString("name"),
                    email = data.optString("email"),
                    role = data.optString("role"),
                    college = data.optString("college")
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun saveSession(token: String, user: User, provider: String = "legacy") {
        prefs.edit()
            .putString(Constants.KEY_TOKEN, token)
            .putString(Constants.KEY_AUTH_PROVIDER, provider)
            .putString(Constants.KEY_USER_ID, user.id)
            .putString(Constants.KEY_USER_ROLE, user.role)
            .putString(Constants.KEY_USER_NAME, user.name)
            .putString(Constants.KEY_USER_EMAIL, user.email)
            // Authentication-dependent requests start immediately after navigation.
            // Persist synchronously so their interceptor never sees a missing/stale JWT.
            .commit()

        com.abpvt.campusgig_frontend.CampusGigApplication.instance.syncFcmToken()
    }
}
