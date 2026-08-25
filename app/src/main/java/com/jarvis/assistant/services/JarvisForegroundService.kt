package com.jarvis.assistant.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.jarvis.assistant.MainActivity
import com.jarvis.assistant.R
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.CommandRouter
import com.jarvis.assistant.settings.SettingsStore
import com.jarvis.assistant.voice.VoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Optional background presence. Always shows a persistent notification while running.
 *
 * Wake-word mode restarts SpeechRecognizer in a loop and checks whether the transcript
 * contains "jarvis" near the start. This is a practical workaround, not a true low-power
 * hotword engine - it uses more battery and is less accurate than a system-level assistant,
 * since Android doesn't expose that kind of hook to third-party apps. Repeated recognizer
 * errors back off instead of retrying instantly, to avoid a busy-error spin loop.
 */
class JarvisForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "jarvis_foreground"
        private const val NOTIFICATION_ID = 7
        private const val WAKE_WORD = "jarvis"
        private const val NORMAL_DELAY_MS = 600L
        private const val MAX_BACKOFF_MS = 5000L
        private const val POST_SPEECH_BUFFER_MS = 500L

        fun start(context: Context) {
            context.startForegroundService(Intent(context, JarvisForegroundService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, JarvisForegroundService::class.java))
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var listeningJob: Job? = null
    private var voiceManager: VoiceManager? = null
    private var router: CommandRouter? = null

    override fun onCreate() {
        super.onCreate()
        router = CommandRouter(applicationContext)
        voiceManager = VoiceManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification(listening = false))

        val hasMicPermission = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        serviceScope.launch {
            val settings = SettingsStore(applicationContext)
            val wakeWordEnabled = settings.voiceActivationEnabled.first()
            if (wakeWordEnabled && hasMicPermission) {
                startWakeWordLoop()
            }
        }

        return START_STICKY
    }

    private fun startWakeWordLoop() {
        listeningJob?.cancel()
        listeningJob = serviceScope.launch {
            updateNotification(listening = true)
            var consecutiveErrors = 0

            while (true) {
                val heard = listenOnce()

                if (heard == null) {
                    consecutiveErrors++
                    val backoff = (NORMAL_DELAY_MS * consecutiveErrors).coerceAtMost(MAX_BACKOFF_MS)
                    delay(backoff)
                    continue
                }

                consecutiveErrors = 0
                val normalized = heard.trim().lowercase().trim('.', ',', '!', '?', ' ')

                if (normalized.contains(WAKE_WORD)) {
                    val afterWakeWord = normalized.substringAfter(WAKE_WORD).trim(',', ' ', '.', '!', '?')
                    if (afterWakeWord.isNotBlank()) {
                        val result = router?.route(afterWakeWord)
                        val response = when (result) {
                            is CommandResult.Success -> result.spokenResponse
                            is CommandResult.NeedsPermission -> result.spokenResponse
                            is CommandResult.NeedsConfirmation -> result.spokenResponse
                            is CommandResult.Unsupported -> result.spokenResponse
                            is CommandResult.Error -> result.spokenResponse
                            null -> null
                        }
                        response?.let { voiceManager?.speakAndAwait(it) }
                        delay(POST_SPEECH_BUFFER_MS)
                    } else {
                        voiceManager?.speakAndAwait("Yes?")
                        delay(POST_SPEECH_BUFFER_MS)
                    }
                }

                delay(NORMAL_DELAY_MS)
            }
        }
    }

    private suspend fun listenOnce(): String? = suspendCancellableCoroutine { cont ->
        voiceManager?.startListening(
            onResult = { text -> if (cont.isActive) cont.resumeWith(Result.success(text)) },
            onError = { if (cont.isActive) cont.resumeWith(Result.success(null)) },
        )
    }

    fun stopWakeWordLoop() {
        listeningJob?.cancel()
        listeningJob = null
        voiceManager?.stopListening()
        updateNotification(listening = false)
    }

    private fun updateNotification(listening: Boolean) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(listening))
    }

    private fun buildNotification(listening: Boolean): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.foreground_service_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.foreground_service_channel_desc)
            }
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 0, openAppIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (listening) "J.A.R.V.I.S. is listening for \"Jarvis\"" else "J.A.R.V.I.S. is standing by")
            .setContentText("Tap to open the assistant")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        listeningJob?.cancel()
        voiceManager?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
