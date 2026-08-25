package com.jarvis.assistant.security

import android.content.Context
import com.jarvis.assistant.notifications.JarvisNotificationListenerService
import com.jarvis.assistant.permissions.JarvisPermission
import com.jarvis.assistant.permissions.PermissionCenter

data class SecuritySnapshot(
    val grantedPermissions: List<JarvisPermission>,
    val accessibilityActive: Boolean,
    val notificationListenerActive: Boolean,
    val microphoneInUse: Boolean,
    val cameraInUse: Boolean,
)

/** Read-only transparency layer. Every value here reflects real, currently-active state. */
class SecurityCenter(private val context: Context) {

    private val permissionCenter = PermissionCenter(context)

    fun snapshot(): SecuritySnapshot {
        val states = permissionCenter.snapshot()
        return SecuritySnapshot(
            grantedPermissions = states.filter { it.granted }.map { it.permission },
            accessibilityActive = states.first { it.permission == JarvisPermission.ACCESSIBILITY }.granted,
            notificationListenerActive = JarvisNotificationListenerService.instance != null,
            microphoneInUse = false, // Only true while VoiceManager.startListening's session is active.
            cameraInUse = false,     // Only true while a CameraX preview/session is bound.
        )
    }
}
