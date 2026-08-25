package com.jarvis.assistant.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.jarvis.assistant.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Restarts the foreground service after a reboot - but only if the user previously opted in
 * via the Background Mode toggle in Settings. Never starts background behavior silently
 * by default.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = SettingsStore(context)
                val enabled = settings.backgroundModeEnabled.first()
                if (enabled) {
                    JarvisForegroundService.start(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
