package com.jarvis.assistant.ai

import android.content.Context
import com.jarvis.assistant.battery.BatteryCommand
import com.jarvis.assistant.commands.AppLaunchCommand
import com.jarvis.assistant.commands.ClearHistoryVoiceCommand
import com.jarvis.assistant.commands.CoinFlipCommand
import com.jarvis.assistant.commands.DeviceInfoCommand
import com.jarvis.assistant.commands.DiceRollCommand
import com.jarvis.assistant.commands.DoNotDisturbCommand
import com.jarvis.assistant.commands.EmailComposeCommand
import com.jarvis.assistant.commands.FlashlightCommand
import com.jarvis.assistant.commands.GreetingCommand
import com.jarvis.assistant.commands.JokeCommand
import com.jarvis.assistant.commands.LockScreenCommand
import com.jarvis.assistant.commands.MapsNavigateCommand
import com.jarvis.assistant.commands.MathCommand
import com.jarvis.assistant.commands.MoreSettingsShortcutCommand
import com.jarvis.assistant.commands.NavigationCommand
import com.jarvis.assistant.commands.PlayStoreSearchCommand
import com.jarvis.assistant.commands.QuoteCommand
import com.jarvis.assistant.commands.RandomNumberCommand
import com.jarvis.assistant.commands.RingerModeCommand
import com.jarvis.assistant.commands.SettingsShortcutCommand
import com.jarvis.assistant.commands.SmsComposeCommand
import com.jarvis.assistant.commands.StorageInfoCommand
import com.jarvis.assistant.commands.TemperatureConvertCommand
import com.jarvis.assistant.commands.TimeDateCommand
import com.jarvis.assistant.commands.WebSearchCommand
import com.jarvis.assistant.commands.YouTubeSearchCommand
import com.jarvis.assistant.location.LocationCommand
import com.jarvis.assistant.media.MediaControlCommand
import com.jarvis.assistant.network.NetworkCommand
import com.jarvis.assistant.notifications.NotificationSummaryCommand
import com.jarvis.assistant.phone.CallCommand
import com.jarvis.assistant.screen.ScreenshotCommand
import com.jarvis.assistant.utils.CommandHistoryStore
import java.util.Locale

/**
 * Central dispatcher: Voice/Text -> CommandRouter -> Android Capability -> CommandResult.
 * New capabilities are added by implementing [JarvisCommand] and registering an instance here.
 */
class CommandRouter(private val context: Context) {

    private val history = CommandHistoryStore(context)
    private val aiProvider = AiProviderClient(context)

    private val commands: List<JarvisCommand> = listOf(
        AppLaunchCommand(context),
        NavigationCommand(context),
        SettingsShortcutCommand(context),
        BatteryCommand(context),
        NetworkCommand(context),
        MediaControlCommand(context),
        LocationCommand(context),
        CallCommand(context),
        ScreenshotCommand(context),
        NotificationSummaryCommand(context),
        FlashlightCommand(context),
        TimeDateCommand(context),
        DoNotDisturbCommand(context),
        LockScreenCommand(context),
        MathCommand(context),
        CoinFlipCommand(context),
        DiceRollCommand(context),
        RandomNumberCommand(context),
        JokeCommand(context),
        QuoteCommand(context),
        GreetingCommand(context),
        TemperatureConvertCommand(context),
        WebSearchCommand(context),
        YouTubeSearchCommand(context),
        MapsNavigateCommand(context),
        SmsComposeCommand(context),
        EmailComposeCommand(context),
        PlayStoreSearchCommand(context),
        MoreSettingsShortcutCommand(context),
        RingerModeCommand(context),
        DeviceInfoCommand(context),
        StorageInfoCommand(context),
        ClearHistoryVoiceCommand(context),
    )

    suspend fun route(rawInput: String): CommandResult {
        val input = normalize(rawInput)
        if (input.isBlank()) {
            return CommandResult.Error("I didn't catch a command.")
        }

        val command = commands.firstOrNull { it.matches(input) }

        val result: CommandResult = if (command != null) {
            try {
                command.execute(input)
            } catch (t: Throwable) {
                CommandResult.Error("That operation failed unexpectedly.", t)
            }
        } else {
            // No built-in command matched - fall back to the configured AI provider
            // for open-ended questions and conversation, if the user has set one up.
            when (val aiResult = aiProvider.askDetailed(rawInput)) {
                is AiProviderClient.AiResult.Success -> CommandResult.Success(aiResult.text)
                is AiProviderClient.AiResult.Failure -> CommandResult.Error(aiResult.reason)
            }
        }

        history.record(
            userCommand = rawInput,
            action = command?.describe(input) ?: "AI query",
            result = resultLabel(result),
        )

        return result
    }

    private fun resultLabel(result: CommandResult): String = when (result) {
        is CommandResult.Success -> "SUCCESS"
        is CommandResult.NeedsPermission -> "NEEDS_PERMISSION"
        is CommandResult.NeedsConfirmation -> "NEEDS_CONFIRMATION"
        is CommandResult.Unsupported -> "UNSUPPORTED"
        is CommandResult.Error -> "ERROR"
    }

    /** Strips the wake word and normalizes casing so pattern matching is consistent. */
    private fun normalize(input: String): String =
        input.trim()
            .lowercase(Locale.getDefault())
            .removePrefix("jarvis,")
            .removePrefix("jarvis")
            .removePrefix("j.a.r.v.i.s.,")
            .removePrefix("j.a.r.v.i.s.")
            .trim()
}
