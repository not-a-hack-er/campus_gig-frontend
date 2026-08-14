package com.abpvt.campusgig_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.abpvt.campusgig_frontend.core.network.AuthInterceptor
import com.abpvt.campusgig_frontend.features.chat.SocketManager
import com.abpvt.campusgig_frontend.navigation.AppNavGraph
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.Campusgig_frontendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CampusGigApplication
        val startDestination = if (app.authRepository.isLoggedIn()) Routes.HOME else Routes.LOGIN

        setContent {
            Campusgig_frontendTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // ── Session Expiry Handler ────────────────────────────────
                    // If the JWT is rejected anywhere in the app (HTTP 401),
                    // AuthInterceptor emits this event. We observe it here to
                    // immediately navigate back to Login and clear the back stack.
                    LaunchedEffect(Unit) {
                        AuthInterceptor.sessionExpiredEvent.collect {
                            SocketManager.disconnect()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}