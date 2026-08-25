package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.commands.accessibilitySettingsIntent
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisText
import com.jarvis.assistant.ui.theme.JarvisTextMuted

@Composable
fun AccessibilitySetupScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    ScreenScaffold(title = "ACCESSIBILITY SETUP", onBack = onBack) {
        Text(
            "Why J.A.R.V.I.S. asks for this",
            style = MaterialTheme.typography.titleMedium,
            color = JarvisText,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "Accessibility Service lets J.A.R.V.I.S. read what's visibly on screen and perform " +
                "taps, scrolls, and back/home navigation - but only when you ask it to, in direct " +
                "response to a command. It never acts on its own, never reads password fields, and " +
                "never reads anything outside a command you gave it.",
            style = MaterialTheme.typography.bodyMedium,
            color = JarvisTextMuted,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = { context.startActivity(accessibilitySettingsIntent()) }) {
            Text("Open Accessibility Settings", color = JarvisCyan)
        }
    }
}
