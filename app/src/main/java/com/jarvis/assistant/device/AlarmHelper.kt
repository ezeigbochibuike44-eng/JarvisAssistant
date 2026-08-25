package com.jarvis.assistant.device

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock

/** Delegates alarm creation to the user's default clock app via the public AlarmClock intent API. */
class AlarmHelper(private val context: Context) {

    fun setAlarm(hour: Int, minute: Int, label: String = "J.A.R.V.I.S."): Boolean {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
