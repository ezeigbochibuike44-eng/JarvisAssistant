package com.jarvis.assistant.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ai.AssistantState
import com.jarvis.assistant.ui.AssistantViewModel
import com.jarvis.assistant.ui.JarvisCore
import com.jarvis.assistant.ui.theme.*

data class QuickAction(val label: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun HomeScreen(
    viewModel: AssistantViewModel,
    onNavigate: (String) -> Unit,
    onScreenshotRequested: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    val requestMicPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.startListening()
    }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StatusBarRow(batteryPercent = uiState.batteryPercent, networkLabel = uiState.networkLabel)

        Spacer(Modifier.height(24.dp))

        Text("J.A.R.V.I.S.", style = MaterialTheme.typography.headlineMedium, color = JarvisText)
        Text(
            text = uiState.assistantState.name,
            style = MaterialTheme.typography.labelSmall,
            color = JarvisCyanDim,
        )

        Spacer(Modifier.height(16.dp))

        Box(contentAlignment = Alignment.Center) {
            JarvisCore(state = uiState.assistantState)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = uiState.lastResponse,
            style = MaterialTheme.typography.bodyMedium,
            color = JarvisText,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        if (uiState.transcript.isNotBlank() && uiState.assistantState == AssistantState.LISTENING) {
            Text(
                text = "\"${uiState.transcript}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = JarvisAmber,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        uiState.pendingConfirmation?.let {
            Spacer(Modifier.height(12.dp))
            Row {
                Button(onClick = { viewModel.confirmPending() }, colors = ButtonDefaults.buttonColors(containerColor = JarvisCyanDim)) {
                    Text("Confirm")
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(onClick = { viewModel.dismissPending() }) {
                    Text("Cancel", color = JarvisTextMuted)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        FloatingActionButton(
            onClick = {
                val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (granted) viewModel.startListening() else requestMicPermission.launch(android.Manifest.permission.RECORD_AUDIO)
            },
            containerColor = if (uiState.assistantState == AssistantState.LISTENING) JarvisAmber else JarvisCyanDim,
            contentColor = JarvisBackground,
        ) {
            Icon(Icons.Filled.Mic, contentDescription = "Push to talk")
        }

        Spacer(Modifier.height(28.dp))

        Text("QUICK ACTIONS", style = MaterialTheme.typography.labelSmall, color = JarvisTextMuted)
        Spacer(Modifier.height(10.dp))

        val actions = listOf(
            QuickAction("Camera", Icons.Filled.CameraAlt) { onNavigate("camera") },
            QuickAction("Screenshot", Icons.Filled.PhotoCamera) { onScreenshotRequested() },
            QuickAction("Battery", Icons.Filled.BatteryFull) { viewModel.submitCommand("battery") },
            QuickAction("Notifications", Icons.Filled.Notifications) { viewModel.submitCommand("read my notifications") },
            QuickAction("Location", Icons.Filled.LocationOn) { viewModel.submitCommand("where am i") },
            QuickAction("Files", Icons.Filled.Folder) { onNavigate("files") },
            QuickAction("Calendar", Icons.Filled.CalendarMonth) { onNavigate("calendar") },
            QuickAction("Settings", Icons.Filled.Settings) { onNavigate("settings") },
            QuickAction("Permissions", Icons.Filled.Security) { onNavigate("permissions") },
            QuickAction("Security", Icons.Filled.Shield) { onNavigate("security") },
            QuickAction("History", Icons.Filled.History) { onNavigate("history") },
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(actions) { action -> QuickActionCard(action) }
        }
    }
}

@Composable
private fun StatusBarRow(batteryPercent: Int, networkLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = if (batteryPercent >= 0) "BATT $batteryPercent%" else "BATT --",
            style = MaterialTheme.typography.labelSmall,
            color = JarvisCyan,
        )
        Text(
            text = networkLabel.ifBlank { "..." }.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = JarvisCyan,
        )
    }
}

@Composable
private fun QuickActionCard(action: QuickAction) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(JarvisPanel)
            .clickable(onClick = action.onClick)
            .padding(vertical = 14.dp, horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(action.icon, contentDescription = action.label, tint = JarvisCyan)
        Spacer(Modifier.height(6.dp))
        Text(action.label, style = MaterialTheme.typography.labelSmall, color = JarvisTextMuted, fontWeight = FontWeight.Medium)
    }
}


