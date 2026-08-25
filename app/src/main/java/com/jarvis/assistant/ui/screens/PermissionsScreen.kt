package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.permissions.JarvisPermission
import com.jarvis.assistant.permissions.PermissionCenter
import com.jarvis.assistant.permissions.PermissionState
import com.jarvis.assistant.ui.theme.*

@Composable
fun PermissionsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val center = remember { PermissionCenter(context) }
    var states by remember { mutableStateOf(center.snapshot()) }

    ScreenScaffold(title = "PERMISSION CENTER", onBack = onBack) {
        Text(
            "Sensitive permissions are only requested when the matching feature is used.",
            style = MaterialTheme.typography.bodyMedium,
            color = JarvisTextMuted,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(states) { state ->
                PermissionRow(state) {
                    val intent = android.content.Intent(center.settingsDestinationFor(state.permission)).apply {
                        if (center.settingsDestinationFor(state.permission) == android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = { states = center.snapshot() }, modifier = Modifier.fillMaxWidth()) {
            Text("Refresh", color = JarvisCyan)
        }
    }
}

@Composable
private fun PermissionRow(state: PermissionState, onFix: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisPanel)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(state.permission.label, color = JarvisText, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (state.granted) "GRANTED" else "NOT GRANTED",
                color = if (state.granted) JarvisCyan else JarvisError,
                style = MaterialTheme.typography.labelSmall,
            )
            if (!state.granted) {
                Spacer(Modifier.width(10.dp))
                TextButton(onClick = onFix) { Text("Fix", color = JarvisAmber) }
            }
        }
    }
}
