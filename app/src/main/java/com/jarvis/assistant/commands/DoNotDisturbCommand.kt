package com.jarvis.assistant.commands

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

/** Toggles Do Not Disturb. Requires the user to grant Notification Policy Access separately - a distinct, more sensitive permission than basic notification listening. */
class DoNotDisturbCommand(private val context: Context) : JarvisCommand {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val patterns = listOf("do not disturb on", "do not disturb off", "enable do not disturb", "disable do not disturb", "dnd on", "dnd off")

    override fun matches(input: String): Boolean = patterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            return CommandResult.NeedsPermission(
                permission = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
                spokenResponse = "I need Do Not Disturb access permission first."
            )
        }
        val turnOn = input.contains("on") || input.contains("enable")
        return try {
            notificationManager.setInterruptionFilter(
                if (turnOn) NotificationManager.INTERRUPTION_FILTER_PRIORITY else NotificationManager.INTERRUPTION_FILTER_ALL
            )
            CommandResult.Success(if (turnOn) "Do Not Disturb enabled." else "Do Not Disturb disabled.")
        } catch (e: Exception) {
            CommandResult.Error("I couldn't change Do Not Disturb.", e)
        }
    }

    override fun describe(input: String): String = input.replaceFirstChar { it.uppercase() }
}

fun notificationPolicyAccessIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
