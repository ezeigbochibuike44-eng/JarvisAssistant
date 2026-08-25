package com.jarvis.assistant.notifications

import android.content.Context
import android.provider.Settings
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

class NotificationSummaryCommand(private val context: Context) : JarvisCommand {

    private val patterns = listOf("read my notifications", "notifications", "what are my notifications")

    override fun matches(input: String): Boolean = patterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        val service = JarvisNotificationListenerService.instance
            ?: return CommandResult.NeedsPermission(
                permission = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS,
                spokenResponse = "I need Notification Access permission first."
            )

        val notifications = service.currentNotifications()
        if (notifications.isEmpty()) {
            return CommandResult.Success("You have no active notifications.")
        }

        val summary = notifications.take(5).joinToString("; ") { n ->
            "${n.title ?: "Notification"}${n.text?.let { ": $it" } ?: ""}"
        }
        return CommandResult.Success(
            spokenResponse = "You have ${notifications.size} notification${if (notifications.size == 1) "" else "s"}.",
            detail = summary
        )
    }

    override fun describe(input: String): String = "Read notifications"
}
