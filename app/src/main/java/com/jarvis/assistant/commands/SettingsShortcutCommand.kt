package com.jarvis.assistant.commands

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

/** Opens the correct system settings screen. Android rarely allows silent changes, so J.A.R.V.I.S. is honest about that. */
class SettingsShortcutCommand(private val context: Context) : JarvisCommand {

    private val destinations = mapOf(
        "wifi settings" to Settings.ACTION_WIFI_SETTINGS,
        "wi-fi settings" to Settings.ACTION_WIFI_SETTINGS,
        "bluetooth settings" to Settings.ACTION_BLUETOOTH_SETTINGS,
        "display settings" to Settings.ACTION_DISPLAY_SETTINGS,
        "sound settings" to Settings.ACTION_SOUND_SETTINGS,
        "battery settings" to Settings.ACTION_BATTERY_SAVER_SETTINGS,
        "application settings" to Settings.ACTION_APPLICATION_SETTINGS,
        "app settings" to Settings.ACTION_APPLICATION_SETTINGS,
        "accessibility settings" to Settings.ACTION_ACCESSIBILITY_SETTINGS,
        "location settings" to Settings.ACTION_LOCATION_SOURCE_SETTINGS,
        "settings" to Settings.ACTION_SETTINGS,
    )

    override fun matches(input: String): Boolean =
        input.startsWith("open ") && destinations.keys.any { input.removePrefix("open ").trim() == it }

    override suspend fun execute(input: String): CommandResult {
        val key = input.removePrefix("open ").trim()
        val action = destinations[key] ?: return CommandResult.Unsupported("I don't recognize that settings page.")

        val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            CommandResult.Success("Opening ${key.replaceFirstChar { it.uppercase() }}.")
        } catch (e: Exception) {
            CommandResult.Unsupported("That operation isn't supported on this Android version.")
        }
    }

    override fun describe(input: String): String = input.replaceFirstChar { it.uppercase() }
}
