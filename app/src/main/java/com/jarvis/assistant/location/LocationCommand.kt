package com.jarvis.assistant.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager as AndroidLocationManager
import androidx.core.content.ContextCompat
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** Reads the last known location from Android's platform LocationManager. No Play Services required. */
class LocationCommand(private val context: Context) : JarvisCommand {

    private val patterns = listOf("where am i", "my location", "current location")

    override fun matches(input: String): Boolean = patterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            return CommandResult.NeedsPermission(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                spokenResponse = "I need location permission before I can find you."
            )
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
        val providers = lm.getProviders(true)

        val location = providers
            .mapNotNull { provider -> runCatching { lm.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
            ?: return CommandResult.Error("I don't have a recent location fix yet. Try again in a moment.")

        return CommandResult.Success(
            spokenResponse = "You are currently at your available GPS location.",
            detail = "Lat ${"%.5f".format(location.latitude)}, Lon ${"%.5f".format(location.longitude)}"
        )
    }

    override fun describe(input: String): String = "Get current location"
}
