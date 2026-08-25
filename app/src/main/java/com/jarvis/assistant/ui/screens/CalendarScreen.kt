package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.calendar.CalendarManager
import com.jarvis.assistant.calendar.JarvisCalendarEvent
import com.jarvis.assistant.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val manager = remember { CalendarManager(context) }
    var events by remember { mutableStateOf<List<JarvisCalendarEvent>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        events = manager.eventsToday()
        loaded = true
    }

    ScreenScaffold(title = "TODAY'S CALENDAR", onBack = onBack) {
        if (!manager.hasReadPermission()) {
            Text(
                "I need Calendar permission before I can read your events.",
                color = JarvisError,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else if (loaded && events.isEmpty()) {
            Text("Nothing on your calendar today.", color = JarvisTextMuted, style = MaterialTheme.typography.bodyMedium)
        } else {
            val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(events) { event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisPanel)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(event.title, color = JarvisText, style = MaterialTheme.typography.bodyMedium)
                        Text(formatter.format(event.startMillis), color = JarvisCyan, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
