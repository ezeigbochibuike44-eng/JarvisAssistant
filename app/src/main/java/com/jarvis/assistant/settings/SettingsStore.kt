package com.jarvis.assistant.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "jarvis_settings")

/** AI provider config the user supplies themselves - never hard-coded into the app. */
class SettingsStore(private val context: Context) {

    private object Keys {
        val AI_PROVIDER_ENDPOINT = stringPreferencesKey("ai_provider_endpoint")
        val AI_PROVIDER_API_KEY = stringPreferencesKey("ai_provider_api_key")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val VOICE_RESPONSES_ENABLED = booleanPreferencesKey("voice_responses_enabled")
        val VOICE_SPEED = floatPreferencesKey("voice_speed")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")
        val CLOUD_HISTORY_SYNC = booleanPreferencesKey("cloud_history_sync")
        val NOTIFICATIONS_TO_CLOUD_AI = booleanPreferencesKey("notifications_to_cloud_ai")
        val BACKGROUND_MODE_ENABLED = booleanPreferencesKey("background_mode_enabled")
        val VOICE_ACTIVATION_ENABLED = booleanPreferencesKey("voice_activation_enabled")
    }

    val aiEndpoint: Flow<String> = context.dataStore.data.map { it[Keys.AI_PROVIDER_ENDPOINT] ?: "" }
    val aiApiKey: Flow<String> = context.dataStore.data.map { it[Keys.AI_PROVIDER_API_KEY] ?: "" }
    val aiModel: Flow<String> = context.dataStore.data.map { it[Keys.AI_MODEL] ?: "" }
    val voiceResponsesEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.VOICE_RESPONSES_ENABLED] ?: true }
    val voiceSpeed: Flow<Float> = context.dataStore.data.map { it[Keys.VOICE_SPEED] ?: 1.0f }
    val animationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.ANIMATIONS_ENABLED] ?: true }
    val cloudHistorySync: Flow<Boolean> = context.dataStore.data.map { it[Keys.CLOUD_HISTORY_SYNC] ?: false }
    val notificationsToCloudAi: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS_TO_CLOUD_AI] ?: false }
    val backgroundModeEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BACKGROUND_MODE_ENABLED] ?: false }
    val voiceActivationEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.VOICE_ACTIVATION_ENABLED] ?: false }

    suspend fun setAiEndpoint(value: String) = context.dataStore.edit { it[Keys.AI_PROVIDER_ENDPOINT] = value }
    suspend fun setAiApiKey(value: String) = context.dataStore.edit { it[Keys.AI_PROVIDER_API_KEY] = value }
    suspend fun setAiModel(value: String) = context.dataStore.edit { it[Keys.AI_MODEL] = value }
    suspend fun setVoiceResponsesEnabled(value: Boolean) = context.dataStore.edit { it[Keys.VOICE_RESPONSES_ENABLED] = value }
    suspend fun setVoiceSpeed(value: Float) = context.dataStore.edit { it[Keys.VOICE_SPEED] = value }
    suspend fun setAnimationsEnabled(value: Boolean) = context.dataStore.edit { it[Keys.ANIMATIONS_ENABLED] = value }
    suspend fun setCloudHistorySync(value: Boolean) = context.dataStore.edit { it[Keys.CLOUD_HISTORY_SYNC] = value }
    suspend fun setNotificationsToCloudAi(value: Boolean) = context.dataStore.edit { it[Keys.NOTIFICATIONS_TO_CLOUD_AI] = value }
    suspend fun setBackgroundModeEnabled(value: Boolean) = context.dataStore.edit { it[Keys.BACKGROUND_MODE_ENABLED] = value }
    suspend fun setVoiceActivationEnabled(value: Boolean) = context.dataStore.edit { it[Keys.VOICE_ACTIVATION_ENABLED] = value }
}
