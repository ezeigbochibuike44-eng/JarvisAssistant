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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.utils.CommandHistoryEntry
import com.jarvis.assistant.utils.CommandHistoryStore
import com.jarvis.assistant.ui.theme.*

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { CommandHistoryStore(context) }
    var entries by remember { mutableStateOf(store.all()) }

    ScreenScaffold(title = "COMMAND HISTORY", onBack = onBack) {
        if (entries.isEmpty()) {
            Text("No commands yet.", color = JarvisTextMuted, style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(entries) { entry -> HistoryRow(entry) }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { store.clear(); entries = store.all() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Clear History", color = JarvisError)
        }
    }
}

@Composable
private fun HistoryRow(entry: CommandHistoryEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(JarvisPanel)
            .padding(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(entry.timestamp, color = JarvisCyanDim, style = MaterialTheme.typography.labelSmall)
            Text(
                entry.result,
                color = if (entry.result == "SUCCESS") JarvisCyan else JarvisAmber,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(entry.action, color = JarvisText, style = MaterialTheme.typography.bodyMedium)
    }
}
