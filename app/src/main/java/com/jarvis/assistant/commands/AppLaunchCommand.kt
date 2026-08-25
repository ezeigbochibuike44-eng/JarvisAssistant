package com.jarvis.assistant.commands

import android.content.Context
import android.content.Intent
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

/**
 * Launches installed applications via PackageManager's public launch-intent API.
 * Never touches another app's internals - it only starts its declared launcher activity.
 */
class AppLaunchCommand(private val context: Context) : JarvisCommand {

    private val triggerWords = listOf("open ", "launch ", "start ")

    override fun matches(input: String): Boolean =
        triggerWords.any { input.startsWith(it) } && extractAppName(input) != null

    override suspend fun execute(input: String): CommandResult {
        val appName = extractAppName(input)
            ?: return CommandResult.Error("I couldn't tell which app to open.")

        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(0)

        val target = installedApps.firstOrNull { appInfo ->
            val label = pm.getApplicationLabel(appInfo).toString().lowercase()
            label.contains(appName) || appName.contains(label)
        } ?: return CommandResult.Unsupported("I couldn't find an app called \"$appName\".")

        val launchIntent = pm.getLaunchIntentForPackage(target.packageName)
            ?: return CommandResult.Unsupported("\"$appName\" doesn't expose a launchable screen.")

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)

        val label = pm.getApplicationLabel(target).toString()
        return CommandResult.Success("Opening $label.")
    }

    override fun describe(input: String): String =
        "Open ${extractAppName(input) ?: "app"}"

    private fun extractAppName(input: String): String? {
        for (trigger in triggerWords) {
            if (input.startsWith(trigger)) {
                val rest = input.removePrefix(trigger).trim()
                return rest.ifBlank { null }
            }
        }
        return null
    }
}
