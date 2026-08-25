package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.settings.SettingsStore
import com.jarvis.assistant.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenAccessibilitySetup: () -> Unit) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }
    val scope = rememberCoroutineScope()

    var endpoint by remember { mutableStateOf("") }
    var voiceResponses by remember { mutableStateOf(true) }
    var animations by remember { mutableStateOf(true) }
    var cloudSync by remember { mutableStateOf(false) }
    var notificationsToCloud by remember { mutableStateOf(false) }
    var backgroundMode by remember { mutableStateOf(false) }
    var voiceActivation by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    var modelInput by remember { mutableStateOf("") }

    val micPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceActivation = true
            scope.launch { store.setVoiceActivationEnabled(true) }
            scope.launch { store.setBackgroundModeEnabled(true) }
            com.jarvis.assistant.services.JarvisForegroundService.start(context)
        }
    }

    LaunchedEffect(Unit) {
        store.aiEndpoint.collect { endpoint = it }
    }
    LaunchedEffect(Unit) {
        store.aiApiKey.collect { apiKeyInput = it }
    }
    LaunchedEffect(Unit) {
        store.aiModel.collect { modelInput = it }
    }
    LaunchedEffect(Unit) {
        store.backgroundModeEnabled.collect { backgroundMode = it }
    }
    LaunchedEffect(Unit) {
        store.voiceActivationEnabled.collect { voiceActivation = it }
    }

    ScreenScaffold(title = "SETTINGS", onBack = onBack) {
        val scroll = rememberScrollState()
        Column(modifier = Modifier.verticalScroll(scroll)) {

            SectionLabel("AI")
            SettingsCard {
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = {
                        endpoint = it
                        scope.launch { store.setAiEndpoint(it) }
                    },
                    label = { Text("AI provider endpoint", color = JarvisTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        scope.launch { store.setAiApiKey(it) }
                    },
                    label = { Text("API key", color = JarvisTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = modelInput,
                    onValueChange = {
                        modelInput = it
                        scope.launch { store.setAiModel(it) }
                    },
                    label = { Text("Model name", color = JarvisTextMuted) },
                    placeholder = { Text("llama-3.3-70b-versatile", color = JarvisTextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text(
                    "Unmatched commands are sent here as open-ended questions. Free option: sign up at console.groq.com, use endpoint https://api.groq.com/openai/v1/chat/completions with your Groq key.",
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisTextMuted,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            SectionLabel("Voice")
            SettingsCard {
                ToggleRow("Voice responses", voiceResponses) {
                    voiceResponses = it
                    scope.launch { store.setVoiceResponsesEnabled(it) }
                }
                Spacer(Modifier.height(8.dp))
                ToggleRow("Voice activation (say \"Jarvis\")", voiceActivation) { enabled ->
                    if (enabled) {
                        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            voiceActivation = true
                            scope.launch { store.setVoiceActivationEnabled(true) }
                            scope.launch { store.setBackgroundModeEnabled(true) }
                            com.jarvis.assistant.services.JarvisForegroundService.start(context)
                        } else {
                            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    } else {
                        voiceActivation = false
                        scope.launch { store.setVoiceActivationEnabled(false) }
                        com.jarvis.assistant.services.JarvisForegroundService.stop(context)
                    }
                }
                Text(
                    "Keeps the microphone listening in the background for the word \"Jarvis\" followed by a command. Uses more battery than push-to-talk.",
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisTextMuted,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            SectionLabel("Appearance")
            SettingsCard {
                ToggleRow("HUD animations", animations) {
                    animations = it
                    scope.launch { store.setAnimationsEnabled(it) }
                }
            }

            SectionLabel("Privacy")
            SettingsCard {
                ToggleRow("Sync command history to cloud", cloudSync) {
                    cloudSync = it
                    scope.launch { store.setCloudHistorySync(it) }
                }
                Spacer(Modifier.height(8.dp))
                ToggleRow("Send notification text to cloud AI", notificationsToCloud) {
                    notificationsToCloud = it
                    scope.launch { store.setNotificationsToCloudAi(it) }
                }
            }

            SectionLabel("Device")
            SettingsCard {
                ToggleRow("Run in background", backgroundMode) { enabled ->
                    backgroundMode = enabled
                    scope.launch { store.setBackgroundModeEnabled(enabled) }
                    if (enabled) {
                        com.jarvis.assistant.services.JarvisForegroundService.start(context)
                    } else {
                        com.jarvis.assistant.services.JarvisForegroundService.stop(context)
                    }
                }
                Text(
                    "Keeps J.A.R.V.I.S. running with a persistent notification, and restarts it after your phone reboots.",
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisTextMuted,
                    modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
                )
                TextButton(onClick = onOpenAccessibilitySetup) {
                    Text("Accessibility Service setup", color = JarvisCyan)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = JarvisAmber,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisPanel)
            .padding(14.dp),
        content = content,
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = JarvisText, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = JarvisCyanDim, checkedThumbColor = JarvisCyan),
        )
    }
}
