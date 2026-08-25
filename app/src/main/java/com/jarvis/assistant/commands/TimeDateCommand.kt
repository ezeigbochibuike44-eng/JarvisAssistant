package com.jarvis.assistant.commands

import android.content.Context
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeDateCommand(private val context: Context) : JarvisCommand {

    private val timePatterns = listOf("what time is it", "current time", "what's the time")
    private val datePatterns = listOf("what's the date", "what is the date", "today's date", "what day is it")

    override fun matches(input: String): Boolean =
        timePatterns.any { input.contains(it) } || datePatterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        return if (timePatterns.any { input.contains(it) }) {
            val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
            CommandResult.Success("It's $time.")
        } else {
            val date = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
            CommandResult.Success("Today is $date.")
        }
    }

    override fun describe(input: String): String = if (timePatterns.any { input.contains(it) }) "Check time" else "Check date"
}
