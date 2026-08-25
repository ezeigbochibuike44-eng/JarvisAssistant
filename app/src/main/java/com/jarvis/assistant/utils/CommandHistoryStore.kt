package com.jarvis.assistant.utils

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
data class CommandHistoryEntry(
    val timestamp: String,
    val userCommand: String,
    val action: String,
    val result: String,
)

/**
 * Persists command history locally only. Nothing here is ever uploaded unless the user
 * explicitly enables cloud sync in Settings (not implemented by default).
 */
class CommandHistoryStore(private val context: Context) {

    private val prefs = context.getSharedPreferences("jarvis_history", Context.MODE_PRIVATE)
    private val key = "entries"
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val json = Json { ignoreUnknownKeys = true }

    fun record(userCommand: String, action: String, result: String) {
        val entry = CommandHistoryEntry(
            timestamp = timeFormat.format(System.currentTimeMillis()),
            userCommand = userCommand,
            action = action,
            result = result,
        )
        val updated = listOf(entry) + all()
        val serialized = json.encodeToString(ListSerializer(CommandHistoryEntry.serializer()), updated.take(200))
        prefs.edit().putString(key, serialized).apply()
    }

    fun all(): List<CommandHistoryEntry> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return try {
            json.decodeFromString(ListSerializer(CommandHistoryEntry.serializer()), raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clear() {
        prefs.edit().remove(key).apply()
    }
}
