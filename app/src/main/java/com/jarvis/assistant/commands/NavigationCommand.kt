package com.jarvis.assistant.commands

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.jarvis.assistant.accessibility.JarvisAccessibilityService
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

/**
 * Handles "go home", "go back", and "scroll" commands using the global-action API exposed
 * by an enabled AccessibilityService. Requires the user to have turned the service on.
 */
class NavigationCommand(private val context: Context) : JarvisCommand {

    private val patterns = listOf("go home", "home screen", "go back", "back", "scroll down", "scroll up")

    override fun matches(input: String): Boolean = patterns.any { input == it || input.startsWith(it) }

    override suspend fun execute(input: String): CommandResult {
        if (!isAccessibilityServiceEnabled()) {
            return CommandResult.NeedsPermission(
                permission = Settings.ACTION_ACCESSIBILITY_SETTINGS,
                spokenResponse = "Please enable J.A.R.V.I.S. Accessibility Service first."
            )
        }

        val service = JarvisAccessibilityService.instance
            ?: return CommandResult.Error("Accessibility Service isn't connected right now.")

        val handled = when {
            input.contains("home") -> service.performGoHome()
            input.contains("back") -> service.performGoBack()
            input.contains("scroll down") -> service.performScroll(forward = true)
            input.contains("scroll up") -> service.performScroll(forward = false)
            else -> false
        }

        return if (handled) {
            CommandResult.Success("Done.")
        } else {
            CommandResult.Unsupported("That navigation action isn't available right now.")
        }
    }

    override fun describe(input: String): String = input.replaceFirstChar { it.uppercase() }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
    }
}

/** Helper used by the Settings/Accessibility setup screen to open the system page directly. */
fun accessibilitySettingsIntent(): Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
