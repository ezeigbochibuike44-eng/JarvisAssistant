package com.jarvis.assistant.ai

import android.content.Context
import com.jarvis.assistant.settings.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Talks to an OpenAI-compatible chat completions endpoint (works with OpenAI, Groq,
 * OpenRouter, and most self-hosted/local model servers that mirror that API shape). The
 * user supplies their own endpoint + key in Settings - nothing is hard-coded here.
 */
class AiProviderClient(private val context: Context) {

    private val settings = SettingsStore(context)

    sealed class AiResult {
        data class Success(val text: String) : AiResult()
        data class Failure(val reason: String) : AiResult()
    }

    suspend fun isConfigured(): Boolean = settings.aiEndpoint.first().isNotBlank()

    /** Returns the reply text, or a specific human-readable failure reason - never a silent null. */
    suspend fun askDetailed(prompt: String): AiResult = withContext(Dispatchers.IO) {
        val endpoint = settings.aiEndpoint.first()
        val apiKey = settings.aiApiKey.first()
        if (endpoint.isBlank()) return@withContext AiResult.Failure("No AI endpoint is set in Settings.")

        try {
            val url = URL(endpoint)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            if (apiKey.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
            }
            connection.doOutput = true
            connection.connectTimeout = TimeUnit.SECONDS.toMillis(15).toInt()
            connection.readTimeout = TimeUnit.SECONDS.toMillis(30).toInt()

            val messages = JSONArray().put(
                JSONObject().put("role", "user").put("content", prompt)
            )
            val model = settings.aiModel.first().ifBlank { "llama-3.3-70b-versatile" }
            val body = JSONObject()
                .put("model", model)
                .put("messages", messages)

            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            val code = connection.responseCode
            if (code !in 200..299) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val errorMessage = try {
                    JSONObject(errorBody).optJSONObject("error")?.optString("message")
                } catch (e: Exception) { null } ?: errorBody.take(200)
                return@withContext AiResult.Failure("AI provider returned HTTP $code: $errorMessage")
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)
            val content = json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
            AiResult.Success(content)
        } catch (e: Exception) {
            AiResult.Failure("AI request failed: ${e.javaClass.simpleName} - ${e.message}")
        }
    }

    /** Convenience wrapper for callers that only need the text or null. */
    suspend fun ask(prompt: String): String? = (askDetailed(prompt) as? AiResult.Success)?.text
}
