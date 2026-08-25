package com.jarvis.assistant.commands

import android.content.Context
import android.hardware.camera2.CameraManager
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.JarvisCommand

/** Toggles the rear camera's flash unit as a torch. No camera preview is opened. */
class FlashlightCommand(private val context: Context) : JarvisCommand {

    private var isOn = false
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val torchCameraId: String? by lazy {
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }

    private val patterns = listOf("flashlight on", "flashlight off", "turn on the flashlight", "turn off the flashlight", "torch on", "torch off")

    override fun matches(input: String): Boolean = patterns.any { input.contains(it) }

    override suspend fun execute(input: String): CommandResult {
        val id = torchCameraId ?: return CommandResult.Unsupported("This device doesn't expose a flash unit.")
        val turnOn = input.contains("on")
        return try {
            cameraManager.setTorchMode(id, turnOn)
            isOn = turnOn
            CommandResult.Success(if (turnOn) "Flashlight on." else "Flashlight off.")
        } catch (e: Exception) {
            CommandResult.Error("I couldn't control the flashlight.", e)
        }
    }

    override fun describe(input: String): String = if (input.contains("on")) "Flashlight on" else "Flashlight off"
}
