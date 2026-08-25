package com.jarvis.assistant.permissions

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat

enum class JarvisPermission(val label: String) {
    MICROPHONE("Microphone"),
    CAMERA("Camera"),
    LOCATION("Location"),
    CONTACTS("Contacts"),
    CALENDAR("Calendar"),
    NOTIFICATIONS("Notifications"),
    ACCESSIBILITY("Accessibility"),
    SCREEN_CAPTURE("Screen Capture"),
    PHONE("Phone"),
    FILES_MEDIA("Files/Media"),
}

data class PermissionState(val permission: JarvisPermission, val granted: Boolean)

/** Single source of truth for the Permission Center screen. Never requests anything itself. */
class PermissionCenter(private val context: Context) {

    fun snapshot(): List<PermissionState> = JarvisPermission.entries.map {
        PermissionState(it, isGranted(it))
    }

    fun isGranted(permission: JarvisPermission): Boolean = when (permission) {
        JarvisPermission.MICROPHONE -> runtimeGranted(Manifest.permission.RECORD_AUDIO)
        JarvisPermission.CAMERA -> runtimeGranted(Manifest.permission.CAMERA)
        JarvisPermission.LOCATION -> runtimeGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            runtimeGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        JarvisPermission.CONTACTS -> runtimeGranted(Manifest.permission.READ_CONTACTS)
        JarvisPermission.CALENDAR -> runtimeGranted(Manifest.permission.READ_CALENDAR)
        JarvisPermission.NOTIFICATIONS -> notificationListenerEnabled()
        JarvisPermission.ACCESSIBILITY -> accessibilityServiceEnabled()
        JarvisPermission.SCREEN_CAPTURE -> false // Session-scoped by design; never persists as "granted".
        JarvisPermission.PHONE -> runtimeGranted(Manifest.permission.CALL_PHONE)
        JarvisPermission.FILES_MEDIA -> runtimeGranted(Manifest.permission.READ_MEDIA_IMAGES).let {
            it || runtimeGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /** The system settings screen that resolves this permission, for a "Fix it" button. */
    fun settingsDestinationFor(permission: JarvisPermission): String = when (permission) {
        JarvisPermission.NOTIFICATIONS -> Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        JarvisPermission.ACCESSIBILITY -> Settings.ACTION_ACCESSIBILITY_SETTINGS
        else -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    }

    private fun runtimeGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun accessibilityServiceEnabled(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            .any { it.resolveInfo.serviceInfo.packageName == context.packageName }
    }

    private fun notificationListenerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: ""
        return enabled.contains(context.packageName)
    }
}
