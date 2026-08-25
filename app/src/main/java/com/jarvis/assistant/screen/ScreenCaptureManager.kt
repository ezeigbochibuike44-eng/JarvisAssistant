package com.jarvis.assistant.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.result.ActivityResultLauncher

/**
 * Wraps the official MediaProjection permission flow. Capture only ever starts after the
 * user explicitly approves the system "Start recording or casting?" dialog - there is no
 * hidden or automatic screen recording path.
 */
class ScreenCaptureManager(private val context: Context) {

    private val projectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    /** Call from an Activity to show the system consent dialog. */
    fun requestAuthorization(launcher: ActivityResultLauncher<Intent>) {
        launcher.launch(projectionManager.createScreenCaptureIntent())
    }

    /** After Activity.RESULT_OK, hand the result to the foreground service to begin capture. */
    fun startCapture(context: Context, resultCode: Int, data: Intent) {
        val intent = Intent(context, ScreenCaptureService::class.java).apply {
            putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, data)
        }
        context.startForegroundService(intent)
    }

    fun isAuthorizationResult(resultCode: Int): Boolean = resultCode == Activity.RESULT_OK
}
