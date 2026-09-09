package com.abpvt.campusgig_frontend

import android.os.Bundle
import android.os.Build
import android.content.Intent
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.abpvt.campusgig_frontend.core.network.AuthInterceptor
import com.abpvt.campusgig_frontend.features.chat.SocketManager
import com.abpvt.campusgig_frontend.navigation.AppNavGraph
import com.abpvt.campusgig_frontend.navigation.Routes
import com.abpvt.campusgig_frontend.ui.theme.CampusGigTheme
import com.abpvt.campusgig_frontend.ui.theme.ThemeViewModel
import com.abpvt.campusgig_frontend.ui.theme.ThemeViewModelFactory

class MainActivity : ComponentActivity() {
    private val notificationIntent = MutableStateFlow<Intent?>(null)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationIntent.value = intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        enableEdgeToEdge()

        val app = application as CampusGigApplication
        val isLoggedIn = app.authRepository.isLoggedIn()

        setContent {
            // ── Theme ─────────────────────────────────────────────────────────
            val themeViewModel: ThemeViewModel = viewModel(
                factory = ThemeViewModelFactory(applicationContext)
            )
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            CampusGigTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val pendingNotificationIntent by notificationIntent.collectAsState()

                    LaunchedEffect(pendingNotificationIntent) {
                        val notificationType = pendingNotificationIntent?.getStringExtra("type").orEmpty()
                        if (notificationType.isNotBlank() && isLoggedIn) {
                            val metadata = mapOf(
                                "senderId" to pendingNotificationIntent?.getStringExtra("senderId").orEmpty(),
                                "senderName" to pendingNotificationIntent?.getStringExtra("senderName").orEmpty(),
                                "gigId" to pendingNotificationIntent?.getStringExtra("gigId").orEmpty()
                            )
                            navController.navigate(
                                Routes.notificationDestination(
                                    notificationType,
                                    pendingNotificationIntent?.getStringExtra("referenceId").orEmpty(),
                                    metadata
                                )
                            )
                            notificationIntent.value = null
                        }
                    }

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
                        navController    = navController,
                        startDestination = Routes.SPLASH,
                        isLoggedIn       = isLoggedIn,
                        themeViewModel   = themeViewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationIntent.value = intent
    }
}
