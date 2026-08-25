package com.jarvis.assistant.ai

/** Outcome of a routed command, always truthful about what actually happened. */
sealed class CommandResult {
    data class Success(val spokenResponse: String, val detail: String? = null) : CommandResult()
    data class NeedsPermission(val permission: String, val spokenResponse: String) : CommandResult()
    data class NeedsConfirmation(val spokenResponse: String, val onConfirm: suspend () -> CommandResult) : CommandResult()
    data class Unsupported(val spokenResponse: String) : CommandResult()
    data class Error(val spokenResponse: String, val throwable: Throwable? = null) : CommandResult()
}

/** A single registered capability J.A.R.V.I.S. can perform. */
interface JarvisCommand {
    /** Regex or keyword patterns this command responds to, checked in order by the router. */
    fun matches(input: String): Boolean

    /** Execute the command. Implementations must never claim success they didn't achieve. */
    suspend fun execute(input: String): CommandResult

    /** Short label used in command history, e.g. "Open Chrome". */
    fun describe(input: String): String
}

enum class AssistantState {
    IDLE, LISTENING, THINKING, EXECUTING, RESPONDING, ERROR
}
