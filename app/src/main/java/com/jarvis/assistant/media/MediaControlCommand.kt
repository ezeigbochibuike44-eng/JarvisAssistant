package com.jarvis.assistant.media

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

/**
 * Volume via AudioManager (directly supported), and play/pause/next/previous via
 * synthetic media key events, which Android routes to whichever app owns the active session.
 */
class MediaControlCommand(private val context: Context) : JarvisCommand {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val patterns = listOf(
        "volume down", "volume up", "mute", "turn the volume down", "turn the volume up",
        "pause the music", "pause music", "play music", "resume music",
        "next song", "play the next song", "previous song", "skip"
    )

    override fun matches(input: String): Boolean = patterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        return when {
            input.contains("volume down") -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                CommandResult.Success("Volume lowered.")
            }
            input.contains("volume up") -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                CommandResult.Success("Volume raised.")
            }
            input.contains("mute") -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                CommandResult.Success("Muted.")
            }
            input.contains("pause") -> sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE, "Paused.")
            input.contains("play") || input.contains("resume") -> sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY, "Playing.")
            input.contains("next") || input.contains("skip") -> sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT, "Skipped to next track.")
            input.contains("previous") -> sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS, "Went to previous track.")
            else -> CommandResult.Unsupported("That media control isn't available.")
        }
    }

    private fun sendMediaKey(keyCode: Int, response: String): CommandResult {
        return try {
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            CommandResult.Success(response)
        } catch (e: Exception) {
            CommandResult.Error("I couldn't reach the media session.", e)
        }
    }

    override fun describe(input: String): String = input.replaceFirstChar { it.uppercase() }
}
