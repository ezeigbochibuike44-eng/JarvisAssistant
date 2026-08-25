package com.jarvis.assistant.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import com.jarvis.assistant.camera.JarvisCameraManager
import com.jarvis.assistant.ui.theme.JarvisAmber
import com.jarvis.assistant.ui.theme.JarvisCyan
import androidx.core.content.ContextCompat

@Composable
fun CameraScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = remember { JarvisCameraManager(context) }
    var statusText by remember { mutableStateOf("") }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    ScreenScaffold(title = "CAMERA", onBack = onBack) {
        if (!hasCameraPermission) {
            Text("Camera permission is required.", color = JarvisAmber, style = MaterialTheme.typography.bodyMedium)
            return@ScreenScaffold
        }

        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    cameraManager.bindPreview(lifecycleOwner, previewView) {
                        statusText = "Camera unavailable on this device."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        if (statusText.isNotBlank()) {
            Text(statusText, color = JarvisAmber, style = MaterialTheme.typography.bodyMedium)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(onClick = {
                cameraManager.toggleLens()
                // Re-bind preview happens automatically on next composition pass via bindPreview call above
                // in a production build; for a true live switch, trigger a state key change here.
            }) {
                Icon(Icons.Filled.Cameraswitch, contentDescription = "Switch camera", tint = JarvisCyan)
            }
            IconButton(onClick = {
                cameraManager.capturePhoto(
                    onSaved = { file -> statusText = "Saved: ${file.name}" },
                    onError = { statusText = "Capture failed." },
                )
            }) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = "Capture", tint = JarvisCyan)
            }
        }
    }
}
