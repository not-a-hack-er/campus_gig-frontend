package com.abpvt.campusgig_frontend.core.network

import android.content.Context
import android.util.Log
import com.abpvt.campusgig_frontend.core.utils.Constants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that:
 *  1. Attaches the JWT Bearer token to every outgoing request.
 *  2. Detects HTTP 401 Unauthorized responses (expired / revoked tokens) and
 *     emits a [sessionExpiredEvent] so the UI can navigate back to Login immediately.
 *
 * PRODUCTION SAFETY:
 * Without 401 handling, an expired token causes ALL API calls to silently fail,
 * leaving users stuck in a permanently broken UI state with no path back to login.
 */
class AuthInterceptor(private val context: Context) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"

        /**
         * SharedFlow that emits Unit whenever a 401 Unauthorized response is received.
         * Observe this in your root Composable or MainActivity to auto-navigate to login.
         *
         * Usage in Compose:
         *   LaunchedEffect(Unit) {
         *       AuthInterceptor.sessionExpiredEvent.collect {
         *           navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
         *       }
         *   }
         */
        private val _sessionExpiredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val sessionExpiredEvent = _sessionExpiredEvent.asSharedFlow()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(Constants.KEY_TOKEN, null)

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        val response = chain.proceed(request)

        // ── 401 Unauthorized: Session expired ────────────────────────────────
        // If the server rejects our token on a protected endpoint (NOT an auth endpoint),
        // clear local session and signal the UI to navigate back to the Login screen.
        val path = chain.request().url.encodedPath
        val isAuthEndpoint = path.contains("auth/login") ||
                             path.contains("auth/register") ||
                             path.contains("auth/forgot-password") ||
                             path.contains("auth/verify-otp") ||
                             path.contains("auth/reset-password")

        if (response.code == 401 && !isAuthEndpoint) {
            Log.w(TAG, "401 Unauthorized on protected endpoint — clearing session and signalling logout")
            prefs.edit().clear().apply()
            _sessionExpiredEvent.tryEmit(Unit)
        }

        return response
    }
}

