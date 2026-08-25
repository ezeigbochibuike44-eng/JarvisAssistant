package com.jarvis.assistant.screen

import android.content.Context
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

/**
 * MediaProjection requires an Activity to launch the system consent dialog, which a voice
 * command dispatched from a background context cannot do on its own. This command reports
 * that truthfully and defers the actual capture flow to MainActivity's screenshot button,
 * which calls ScreenCaptureManager directly.
 */
class ScreenshotCommand(private val context: Context) : JarvisCommand {

    private val patterns = listOf("take a screenshot", "screenshot", "capture the screen")

    override fun matches(input: String): Boolean = patterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        return CommandResult.Unsupported(
            "Please authorize screen capture first using the Screenshot quick action - " +
                "Android requires that confirmation to come from an on-screen tap, not voice."
        )
    }

    override fun describe(input: String): String = "Take screenshot"
}
