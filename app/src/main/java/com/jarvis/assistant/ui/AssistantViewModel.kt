package com.jarvis.assistant.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.ai.AssistantState
import com.jarvis.assistant.ai.CommandResult
import com.jarvis.assistant.ai.CommandRouter
import com.jarvis.assistant.battery.BatteryMonitor
import com.jarvis.assistant.network.NetworkMonitor
import com.jarvis.assistant.utils.CommandHistoryEntry
import com.jarvis.assistant.utils.CommandHistoryStore
import com.jarvis.assistant.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssistantUiState(
    val assistantState: AssistantState = AssistantState.IDLE,
    val lastResponse: String = "J.A.R.V.I.S. online.",
    val transcript: String = "",
    val batteryPercent: Int = -1,
    val networkLabel: String = "",
    val pendingConfirmation: (suspend () -> CommandResult)? = null,
)

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val router = CommandRouter(application)
    private val voice = VoiceManager(application)
    private val historyStore = CommandHistoryStore(application)
    private val batteryMonitor = BatteryMonitor(application)
    private val networkMonitor = NetworkMonitor(application)

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private val _history = MutableStateFlow<List<CommandHistoryEntry>>(emptyList())
    val history: StateFlow<List<CommandHistoryEntry>> = _history.asStateFlow()

    init {
        refreshStatus()
        refreshHistory()
    }

    fun refreshStatus() {
        val battery = batteryMonitor.currentStatus()
        val network = networkMonitor.currentStatus()
        _uiState.value = _uiState.value.copy(
            batteryPercent = battery.percent,
            networkLabel = if (network.isConnected) network.transport else "Offline",
        )
    }

    fun refreshHistory() {
        _history.value = historyStore.all()
    }

    fun startListening() {
        if (!voice.isAvailable()) {
            _uiState.value = _uiState.value.copy(
                assistantState = AssistantState.ERROR,
                lastResponse = "Speech recognition isn't available on this device.",
            )
            return
        }
        _uiState.value = _uiState.value.copy(assistantState = AssistantState.LISTENING, lastResponse = "I'm listening.")
        voice.startListening(
            onResult = { text -> submitCommand(text) },
            onError = { message ->
                _uiState.value = _uiState.value.copy(assistantState = AssistantState.ERROR, lastResponse = message)
            },
            onPartial = { partial -> _uiState.value = _uiState.value.copy(transcript = partial) },
        )
    }

    fun submitCommand(text: String) {
        _uiState.value = _uiState.value.copy(assistantState = AssistantState.THINKING, transcript = text)
        viewModelScope.launch {
            val result = router.route(text)
            applyResult(result)
        }
    }

    fun confirmPending() {
        val confirm = _uiState.value.pendingConfirmation ?: return
        _uiState.value = _uiState.value.copy(assistantState = AssistantState.EXECUTING, pendingConfirmation = null)
        viewModelScope.launch {
            val result = confirm()
            applyResult(result)
        }
    }

    fun dismissPending() {
        _uiState.value = _uiState.value.copy(pendingConfirmation = null, assistantState = AssistantState.IDLE)
    }

    private fun applyResult(result: CommandResult) {
        val response = when (result) {
            is CommandResult.Success -> result.spokenResponse
            is CommandResult.NeedsPermission -> result.spokenResponse
            is CommandResult.NeedsConfirmation -> result.spokenResponse
            is CommandResult.Unsupported -> result.spokenResponse
            is CommandResult.Error -> result.spokenResponse
        }

        val state = when (result) {
            is CommandResult.Success -> AssistantState.RESPONDING
            is CommandResult.NeedsConfirmation -> AssistantState.RESPONDING
            else -> AssistantState.ERROR
        }

        _uiState.value = _uiState.value.copy(
            assistantState = state,
            lastResponse = response,
            pendingConfirmation = (result as? CommandResult.NeedsConfirmation)?.onConfirm,
        )
        voice.speak(response)
        refreshStatus()
        refreshHistory()
    }

    override fun onCleared() {
        voice.release()
        super.onCleared()
    }
}
