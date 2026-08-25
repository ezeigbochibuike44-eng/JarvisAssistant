package com.jarvis.assistant.commands

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

/** Additional system settings shortcuts beyond the core set - same honest "open the real page" approach. */
class MoreSettingsShortcutCommand(private val context: Context) : JarvisCommand {

    private val destinations = mapOf(
        "airplane mode settings" to Settings.ACTION_AIRPLANE_MODE_SETTINGS,
        "hotspot settings" to Settings.ACTION_WIRELESS_SETTINGS,
        "tethering settings" to Settings.ACTION_WIRELESS_SETTINGS,
        "storage settings" to Settings.ACTION_INTERNAL_STORAGE_SETTINGS,
        "date settings" to Settings.ACTION_DATE_SETTINGS,
        "date and time settings" to Settings.ACTION_DATE_SETTINGS,
        "security settings" to Settings.ACTION_SECURITY_SETTINGS,
        "notification settings" to Settings.ACTION_APP_NOTIFICATION_SETTINGS,
        "developer options" to Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
        "nfc settings" to Settings.ACTION_NFC_SETTINGS,
        "sync settings" to Settings.ACTION_SYNC_SETTINGS,
        "privacy settings" to Settings.ACTION_PRIVACY_SETTINGS,
        "vpn settings" to Settings.ACTION_VPN_SETTINGS,
        "input settings" to Settings.ACTION_INPUT_METHOD_SETTINGS,
        "keyboard settings" to Settings.ACTION_INPUT_METHOD_SETTINGS,
    )

    override fun matches(input: String): Boolean =
        input.startsWith("open ") && destinations.keys.any { input.removePrefix("open ").trim() == it }

    override suspend fun execute(input: String): CommandResult {
        val key = input.removePrefix("open ").trim()
        val action = destinations[key] ?: return CommandResult.Unsupported("I don't recognize that settings page.")
        return try {
            context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            CommandResult.Success("Opening ${key.replaceFirstChar { it.uppercase() }}.")
        } catch (e: Exception) {
            CommandResult.Unsupported("That settings page isn't available on this Android version.")
        }
    }

    override fun describe(input: String): String = input.replaceFirstChar { it.uppercase() }
}

/** Vibrate/silent/normal ringer mode toggle - genuinely controllable without special permission. */
class RingerModeCommand(private val context: Context) : JarvisCommand {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
    private val patterns = listOf("silent mode on", "silent mode off", "vibrate mode on", "normal mode on", "ringer normal")

    override fun matches(input: String): Boolean = patterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        return try {
            when {
                input.contains("silent mode on") -> {
                    audioManager.ringerMode = android.media.AudioManager.RINGER_MODE_SILENT
                    CommandResult.Success("Silent mode on.")
                }
                input.contains("vibrate mode on") -> {
                    audioManager.ringerMode = android.media.AudioManager.RINGER_MODE_VIBRATE
                    CommandResult.Success("Vibrate mode on.")
                }
                else -> {
                    audioManager.ringerMode = android.media.AudioManager.RINGER_MODE_NORMAL
                    CommandResult.Success("Ringer set to normal.")
                }
            }
        } catch (e: SecurityException) {
            CommandResult.NeedsPermission(
                permission = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS,
                spokenResponse = "I need Do Not Disturb access to change ringer mode on this Android version."
            )
        }
    }

    override fun describe(input: String): String = input.replaceFirstChar { it.uppercase() }
}

class DeviceInfoCommand(private val context: Context) : JarvisCommand {
    override fun matches(input: String): Boolean = input.contains("device info") || input.contains("what phone is this") || input.contains("android version")

    override suspend fun execute(input: String): CommandResult {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        val version = android.os.Build.VERSION.RELEASE
        return CommandResult.Success("This is a $manufacturer $model, running Android $version.")
    }

    override fun describe(input: String): String = "Device info"
}

class StorageInfoCommand(private val context: Context) : JarvisCommand {
    override fun matches(input: String): Boolean = input.contains("storage space") || input.contains("free space") || input.contains("how much storage")

    override suspend fun execute(input: String): CommandResult {
        val stat = android.os.StatFs(context.filesDir.path)
        val freeGb = stat.availableBytes / (1024.0 * 1024.0 * 1024.0)
        val totalGb = stat.totalBytes / (1024.0 * 1024.0 * 1024.0)
        return CommandResult.Success("You have ${"%.1f".format(freeGb)} gigabytes free out of ${"%.1f".format(totalGb)}.")
    }

    override fun describe(input: String): String = "Storage info"
}

class ClearHistoryVoiceCommand(private val context: Context) : JarvisCommand {
    override fun matches(input: String): Boolean = input.contains("clear my command history") || input.contains("clear history")

    override suspend fun execute(input: String): CommandResult {
        com.jarvis.assistant.utils.CommandHistoryStore(context).clear()
        return CommandResult.Success("Command history cleared.")
    }

    override fun describe(input: String): String = "Clear history"
}
