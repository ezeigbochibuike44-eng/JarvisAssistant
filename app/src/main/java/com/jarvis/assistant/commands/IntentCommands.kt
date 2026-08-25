package com.jarvis.assistant.commands

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

/** "Search the web for X" / "Google X" - opens the browser with a search query. */
class WebSearchCommand(private val context: Context) : JarvisCommand {
    private val triggers = listOf("search the web for ", "google ", "search for ", "look up ")
    override fun matches(input: String): Boolean = triggers.any { input.startsWith(it) }
    override suspend fun execute(input: String): CommandResult {
        val query = triggers.firstOrNull { input.startsWith(it) }?.let { input.removePrefix(it) }?.trim()
            ?: return CommandResult.Error("I need something to search for.")
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra("query", query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            CommandResult.Success("Searching for $query.")
        } catch (e: Exception) {
            CommandResult.Unsupported("No browser available to search with.")
        }
    }
    override fun describe(input: String): String = "Web search"
}

/** "Search YouTube for X" / "play X on YouTube". */
class YouTubeSearchCommand(private val context: Context) : JarvisCommand {
    private val triggers = listOf("search youtube for ", "play ")
    override fun matches(input: String): Boolean =
        input.startsWith("search youtube for ") || (input.startsWith("play ") && input.endsWith(" on youtube"))

    override suspend fun execute(input: String): CommandResult {
        val query = input.removePrefix("search youtube for ").removePrefix("play ").removeSuffix(" on youtube").trim()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            CommandResult.Success("Searching YouTube for $query.")
        } catch (e: Exception) {
            CommandResult.Error("Couldn't open YouTube.", e)
        }
    }
    override fun describe(input: String): String = "YouTube search"
}

/** "Navigate to X" / "directions to X" - opens Maps with turn-by-turn navigation. */
class MapsNavigateCommand(private val context: Context) : JarvisCommand {
    private val triggers = listOf("navigate to ", "directions to ", "take me to ")
    override fun matches(input: String): Boolean = triggers.any { input.startsWith(it) }
    override suspend fun execute(input: String): CommandResult {
        val destination = triggers.firstOrNull { input.startsWith(it) }?.let { input.removePrefix(it) }?.trim()
            ?: return CommandResult.Error("I need a destination.")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${Uri.encode(destination)}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            CommandResult.Success("Navigating to $destination.")
        } catch (e: Exception) {
            // Fall back to a plain maps search if no turn-by-turn nav app is installed.
            return try {
                val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(destination)}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallback)
                CommandResult.Success("Showing $destination on the map.")
            } catch (e2: Exception) {
                CommandResult.Unsupported("No maps app available.")
            }
        }
    }
    override fun describe(input: String): String = "Navigate"
}

/** "Text John saying I'm on my way" - opens the SMS composer pre-filled, doesn't send automatically. */
class SmsComposeCommand(private val context: Context) : JarvisCommand {
    private val pattern = Regex("""text (\w+) saying (.+)""")
    override fun matches(input: String): Boolean = pattern.containsMatchIn(input)
    override suspend fun execute(input: String): CommandResult {
        val match = pattern.find(input) ?: return CommandResult.Unsupported("I couldn't parse who to text.")
        val (_, message) = match.destructured
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply {
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            CommandResult.Success("Opening a text message. You'll need to pick the contact and send it.")
        } catch (e: Exception) {
            CommandResult.Unsupported("No messaging app available.")
        }
    }
    override fun describe(input: String): String = "Compose text message"
}

/** "Send an email" - opens the email composer. */
class EmailComposeCommand(private val context: Context) : JarvisCommand {
    override fun matches(input: String): Boolean = input.contains("send an email") || input.contains("compose an email") || input.contains("new email")
    override suspend fun execute(input: String): CommandResult {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            CommandResult.Success("Opening email.")
        } catch (e: Exception) {
            CommandResult.Unsupported("No email app available.")
        }
    }
    override fun describe(input: String): String = "Compose email"
}

/** "Search the play store for X" / "find X on the play store". */
class PlayStoreSearchCommand(private val context: Context) : JarvisCommand {
    private val triggers = listOf("search the play store for ", "find ")
    override fun matches(input: String): Boolean =
        input.startsWith("search the play store for ") || (input.startsWith("find ") && input.endsWith(" on the play store"))

    override suspend fun execute(input: String): CommandResult {
        val query = input.removePrefix("search the play store for ").removePrefix("find ").removeSuffix(" on the play store").trim()
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${Uri.encode(query)}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            CommandResult.Success("Searching the Play Store for $query.")
        } catch (e: Exception) {
            CommandResult.Unsupported("Play Store isn't available.")
        }
    }
    override fun describe(input: String): String = "Play Store search"
}
