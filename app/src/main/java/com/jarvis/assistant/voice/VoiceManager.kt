package com.jarvis.assistant.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID

/**
 * Thin wrapper around Android's built-in speech APIs. No audio is recorded or transmitted
 * outside of what SpeechRecognizer/TextToSpeech do internally.
 *
 * The recognizer is destroyed and recreated on every startListening() call, since Android's
 * SpeechRecognizer frequently throws "busy" errors if reused before its previous session
 * has fully closed.
 *
 * speakAndAwait() is the important piece for a continuous listening loop: it suspends until
 * TTS playback actually finishes, so the caller can wait before re-opening the mic. Without
 * this, the mic reopens immediately while J.A.R.V.I.S. is still talking and picks up its own
 * voice as new input - which looks exactly like random, erratic "misbehaving."
 */
class VoiceManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private val available = SpeechRecognizer.isRecognitionAvailable(context)

    init {
        tts = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) tts?.language = Locale.getDefault()
        }
    }

    fun isAvailable(): Boolean = available

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onPartial: (String) -> Unit = {},
    ) {
        if (!available) return onError("Speech recognition isn't available on this device.")

        speechRecognizer?.destroy()
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: android.os.Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (text != null) onResult(text) else onError("I didn't catch that.")
            }
            override fun onPartialResults(partialResults: android.os.Bundle?) {
                val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (text != null) onPartial(text)
            }
            override fun onError(error: Int) = onError("Speech recognition error ($error).")
            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })

        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            onError("Couldn't start listening: ${e.message}")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    /** Fire-and-forget speak, for UI contexts where nothing needs to wait on completion. */
    fun speak(text: String) {
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_${UUID.randomUUID()}")
        }
    }

    /** Suspends until this utterance has fully finished playing - critical for a listen loop
     *  so the mic doesn't reopen while J.A.R.V.I.S. is still talking. */
    suspend fun speakAndAwait(text: String) {
        val engine = tts
        if (!ttsReady || engine == null) return

        val utteranceId = "jarvis_${UUID.randomUUID()}"
        suspendCancellableCoroutine<Unit> { cont ->
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) {
                    if (id == utteranceId && cont.isActive) cont.resumeWith(Result.success(Unit))
                }
                @Deprecated("Deprecated in Java")
                override fun onError(id: String?) {
                    if (id == utteranceId && cont.isActive) cont.resumeWith(Result.success(Unit))
                }
                override fun onError(id: String?, errorCode: Int) {
                    if (id == utteranceId && cont.isActive) cont.resumeWith(Result.success(Unit))
                }
            })
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts?.shutdown()
    }
}
