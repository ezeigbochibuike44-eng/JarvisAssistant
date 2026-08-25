package com.jarvis.assistant.commands

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.jarvis.assistant.accessibility.JarvisAccessibilityService
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

class LockScreenCommand(private val context: Context) : JarvisCommand {

    private val patterns = listOf("lock the screen", "lock my phone", "lock screen")

    override fun matches(input: String): Boolean = patterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabled = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            .any { it.resolveInfo.serviceInfo.packageName == context.packageName }

        if (!enabled) {
            return CommandResult.NeedsPermission(
                permission = Settings.ACTION_ACCESSIBILITY_SETTINGS,
                spokenResponse = "Please enable J.A.R.V.I.S. Accessibility Service first."
            )
        }

        val service = JarvisAccessibilityService.instance
            ?: return CommandResult.Error("Accessibility Service isn't connected right now.")

        val locked = service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
        return if (locked) CommandResult.Success("Locking the screen.") else CommandResult.Unsupported("This device doesn't support locking via Accessibility.")
    }

    override fun describe(input: String): String = "Lock screen"
}
