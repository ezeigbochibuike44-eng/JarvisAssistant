package com.jarvis.assistant.network

import android.content.Context
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

class NetworkCommand(context: Context) : JarvisCommand {

    private val monitor = NetworkMonitor(context)
    private val patterns = listOf("wifi status", "wi-fi status", "network status", "am i online", "internet connection")

    override fun matches(input: String): Boolean = patterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        val status = monitor.currentStatus()
        return if (status.isConnected) {
            CommandResult.Success("You're connected via ${status.transport}.")
        } else {
            CommandResult.Success("You're currently offline.")
        }
    }

    override fun describe(input: String): String = "Check network status"
}
