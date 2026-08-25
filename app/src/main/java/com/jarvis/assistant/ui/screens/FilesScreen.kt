package com.jarvis.assistant.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisText
import com.jarvis.assistant.ui.theme.JarvisTextMuted

/**
 * Uses the Storage Access Framework document picker - the app never gets broad filesystem
 * access, only the specific file(s) the user selects.
 */
@Composable
fun FilesScreen(onBack: () -> Unit) {
    var selectedName by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedName = uri?.lastPathSegment ?: "No file selected"
    }

    ScreenScaffold(title = "FILES", onBack = onBack) {
        Text(
            "J.A.R.V.I.S. only accesses files you explicitly pick here - never another app's private storage.",
            style = MaterialTheme.typography.bodyMedium,
            color = JarvisTextMuted,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { launcher.launch(arrayOf("*/*")) }) {
            Text("Browse Files", color = JarvisCyan)
        }
        if (selectedName.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text("Selected: $selectedName", color = JarvisText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
