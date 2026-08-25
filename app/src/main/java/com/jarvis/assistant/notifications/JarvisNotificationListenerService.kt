package com.jarvis.assistant.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

data class JarvisNotification(
    val appLabel: String,
    val title: String?,
    val text: String?,
    val postedAt: Long,
)

class JarvisNotificationListenerService : NotificationListenerService() {

    companion object {
        var instance: JarvisNotificationListenerService? = null
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
    }

    override fun onListenerDisconnected() {
        instance = null
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Stored only in-memory via activeNotifications; nothing is persisted or sent
        // off-device here. Cloud summarization is opt-in and handled by a higher layer.
    }

    /** Snapshot of currently visible notifications, read on-demand by NotificationSummaryCommand. */
    fun currentNotifications(): List<JarvisNotification> {
        return try {
            activeNotifications.map { sbn ->
                val extras = sbn.notification.extras
                JarvisNotification(
                    appLabel = sbn.packageName,
                    title = extras.getCharSequence("android.title")?.toString(),
                    text = extras.getCharSequence("android.text")?.toString(),
                    postedAt = sbn.postTime,
                )
            }
        } catch (e: SecurityException) {
            emptyList()
        }
    }
}
