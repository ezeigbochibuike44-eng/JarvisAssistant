package com.jarvis.assistant.battery

import android.content.Context
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

class BatteryCommand(context: Context) : JarvisCommand {

    private val monitor = BatteryMonitor(context)
    private val patterns = listOf("battery", "how much battery", "what's my battery", "battery level")

    override fun matches(input: String): Boolean = patterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        val status = monitor.currentStatus()
        if (status.percent < 0) {
            return CommandResult.Error("I couldn't read the battery level.")
        }
        val chargingNote = if (status.isCharging) " and charging" else ""
        return CommandResult.Success(
            spokenResponse = "You have ${status.percent} percent battery remaining$chargingNote.",
            detail = status.temperatureCelsius?.let { "Temperature: %.1f°C".format(it) }
        )
    }

    override fun describe(input: String): String = "Check battery"
}
