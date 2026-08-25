package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.security.SecurityCenter
import com.jarvis.assistant.ui.theme.*

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val snapshot = remember { SecurityCenter(context).snapshot() }

    ScreenScaffold(title = "SECURITY CENTER", onBack = onBack) {
        SecurityCard("Accessibility Service", snapshot.accessibilityActive)
        SecurityCard("Notification Listener", snapshot.notificationListenerActive)
        SecurityCard("Microphone Active Now", snapshot.microphoneInUse)
        SecurityCard("Camera Active Now", snapshot.cameraInUse)

        Spacer(Modifier.height(16.dp))
        Text("GRANTED PERMISSIONS", style = MaterialTheme.typography.labelSmall, color = JarvisTextMuted)
        Spacer(Modifier.height(8.dp))
        if (snapshot.grantedPermissions.isEmpty()) {
            Text("None granted yet.", color = JarvisTextMuted, style = MaterialTheme.typography.bodyMedium)
        } else {
            snapshot.grantedPermissions.forEach {
                Text("• ${it.label}", color = JarvisText, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SecurityCard(label: String, active: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisPanel)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = JarvisText, style = MaterialTheme.typography.bodyMedium)
        Text(
            if (active) "ACTIVE" else "INACTIVE",
            color = if (active) JarvisAmber else JarvisTextMuted,
            style = MaterialTheme.typography.labelSmall,
        )
    }
    Spacer(Modifier.height(8.dp))
}
