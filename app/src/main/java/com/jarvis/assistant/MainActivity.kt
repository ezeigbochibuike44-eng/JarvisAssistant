package com.jarvis.assistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jarvis.assistant.screen.ScreenCaptureManager
import com.jarvis.assistant.ui.AssistantViewModel
import com.jarvis.assistant.ui.screens.*
import com.jarvis.assistant.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AssistantViewModel by viewModels()
    private lateinit var screenCaptureManager: ScreenCaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenCaptureManager = ScreenCaptureManager(this)

        setContent {
            JarvisTheme {
                val navController = rememberNavController()

                val screenCaptureLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    val data = result.data
                    if (screenCaptureManager.isAuthorizationResult(result.resultCode) && data != null) {
                        screenCaptureManager.startCapture(this, result.resultCode, data)
                    }
                }

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigate = { destination -> navController.navigate(destination) },
                            onScreenshotRequested = { screenCaptureManager.requestAuthorization(screenCaptureLauncher) },
                        )
                    }
                    composable("camera") { CameraScreen(onBack = { navController.popBackStack() }) }
                    composable("files") { FilesScreen(onBack = { navController.popBackStack() }) }
                    composable("calendar") { CalendarScreen(onBack = { navController.popBackStack() }) }
                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onOpenAccessibilitySetup = { navController.navigate("accessibility_setup") },
                        )
                    }
                    composable("permissions") { PermissionsScreen(onBack = { navController.popBackStack() }) }
                    composable("security") { SecurityScreen(onBack = { navController.popBackStack() }) }
                    composable("history") { HistoryScreen(onBack = { navController.popBackStack() }) }
                    composable("accessibility_setup") { AccessibilitySetupScreen(onBack = { navController.popBackStack() }) }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshStatus()
    }
}
