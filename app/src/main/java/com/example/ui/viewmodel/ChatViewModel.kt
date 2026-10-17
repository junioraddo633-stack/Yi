package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiResult
import com.example.data.api.GeminiService
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.repository.ChatRepository
import com.example.util.AudioHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val geminiService = GeminiService()
    private val repository = ChatRepository(database.chatDao(), geminiService)
    val audioHelper = AudioHelper(application)

    val sessions: StateFlow<List<ChatSession>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessage>> = _currentSessionId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getMessages(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingStatus = MutableStateFlow("Nana is thinking…")
    val loadingStatus: StateFlow<String> = _loadingStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Mode toggles
    private val _selectedModel = MutableStateFlow("gemini-3.8-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isThinkingMode = MutableStateFlow(false)
    val isThinkingMode: StateFlow<Boolean> = _isThinkingMode.asStateFlow()

    private val _isSearchGrounding = MutableStateFlow(false)
    val isSearchGrounding: StateFlow<Boolean> = _isSearchGrounding.asStateFlow()

    private val _isMapsGrounding = MutableStateFlow(false)
    val isMapsGrounding: StateFlow<Boolean> = _isMapsGrounding.asStateFlow()

    private val _isAutoSpeakEnabled = MutableStateFlow(false)
    val isAutoSpeakEnabled: StateFlow<Boolean> = _isAutoSpeakEnabled.asStateFlow()

    val isSpeaking = audioHelper.isSpeaking

    private var lastUserPrompt: String? = null

    init {
        viewModelScope.launch {
            sessions.collect { list ->
                if (_currentSessionId.value == null && list.isNotEmpty()) {
                    _currentSessionId.value = list.first().id
                } else if (_currentSessionId.value == null && list.isEmpty()) {
                    val newId = repository.createNewSession("Welcome to Nana AI", _selectedModel.value)
                    _currentSessionId.value = newId
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        audioHelper.stop()
        _errorMessage.value = null
    }

    fun createNewSession() {
        viewModelScope.launch {
            val newId = repository.createNewSession(
                title = "New Chat",
                model = _selectedModel.value
            )
            _currentSessionId.value = newId
            audioHelper.stop()
            _errorMessage.value = null
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                val remaining = sessions.value.filter { it.id != sessionId }
                if (remaining.isNotEmpty()) {
                    _currentSessionId.value = remaining.first().id
                } else {
                    val newId = repository.createNewSession()
                    _currentSessionId.value = newId
                }
            }
        }
    }

    fun clearCurrentSession() {
        val sId = _currentSessionId.value ?: return
        viewModelScope.launch {
            repository.clearMessages(sId)
        }
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
        if (model == "gemini-4-argon") {
            _isThinkingMode.value = true
        } else {
            _isThinkingMode.value = false
        }
    }

    fun toggleThinkingMode(enable: Boolean) {
        _isThinkingMode.value = enable
        if (enable) {
            _selectedModel.value = "gemini-4-argon"
            _isSearchGrounding.value = false
            _isMapsGrounding.value = false
        } else {
            _selectedModel.value = "gemini-3.8-flash"
        }
    }

    fun toggleSearchGrounding(enable: Boolean) {
        _isSearchGrounding.value = enable
        if (enable) {
            _isThinkingMode.value = false
            if (_selectedModel.value == "gemini-4-argon") {
                _selectedModel.value = "gemini-3.8-flash"
            }
        }
    }

    fun toggleMapsGrounding(enable: Boolean) {
        _isMapsGrounding.value = enable
        if (enable) {
            _isThinkingMode.value = false
            if (_selectedModel.value == "gemini-4-argon") {
                _selectedModel.value = "gemini-3.8-flash"
            }
        }
    }

    fun toggleAutoSpeak() {
        _isAutoSpeakEnabled.value = !_isAutoSpeakEnabled.value
        if (!_isAutoSpeakEnabled.value) {
            audioHelper.stop()
        }
    }

    fun speak(text: String) {
        audioHelper.speak(text)
    }

    fun stopSpeaking() {
        audioHelper.stop()
    }

    fun sendMessage(userText: String) {
        val prompt = userText.trim()
        if (prompt.isBlank() || _isLoading.value) return

        lastUserPrompt = prompt
        _errorMessage.value = null
        _isLoading.value = true
        _loadingStatus.value = when {
            _isThinkingMode.value -> "Nana is deep thinking with Gemini 4 Argon…"
            _isSearchGrounding.value -> "Nana is searching the web with Google Search…"
            _isMapsGrounding.value -> "Nana is exploring Google Maps data…"
            _selectedModel.value == "gemini-3.1-flash-lite" -> "Nana is generating lightning response…"
            else -> "Nana is thinking…"
        }

        viewModelScope.launch {
            val sessionId = repository.ensureSessionExists(_currentSessionId.value)
            _currentSessionId.value = sessionId

            val result = repository.sendMessage(
                sessionId = sessionId,
                userText = prompt,
                modelName = _selectedModel.value,
                enableThinking = _isThinkingMode.value,
                enableSearch = _isSearchGrounding.value,
                enableMaps = _isMapsGrounding.value
            )

            _isLoading.value = false

            when (result) {
                is GeminiResult.Success -> {
                    if (_isAutoSpeakEnabled.value) {
                        audioHelper.speak(result.data)
                    }
                }
                is GeminiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun retryLastMessage() {
        val prompt = lastUserPrompt ?: return
        sendMessage(prompt)
    }

    fun generateImage(
        prompt: String,
        model: String = "gemini-3-pro-image-preview",
        size: String = "1K",
        aspectRatio: String = "1:1"
    ) {
        if (prompt.isBlank() || _isLoading.value) return
        _errorMessage.value = null
        _isLoading.value = true
        _loadingStatus.value = "Nana is painting your image with $model ($size)…"

        viewModelScope.launch {
            val sessionId = repository.ensureSessionExists(_currentSessionId.value)
            _currentSessionId.value = sessionId

            val result = repository.generateImage(
                sessionId = sessionId,
                prompt = prompt,
                modelName = model,
                imageSize = size,
                aspectRatio = aspectRatio
            )

            _isLoading.value = false
            if (result is GeminiResult.Error) {
                _errorMessage.value = result.message
            }
        }
    }

    fun generateMusic(
        prompt: String,
        model: String = "lyria-3-clip-preview"
    ) {
        if (prompt.isBlank() || _isLoading.value) return
        _errorMessage.value = null
        _isLoading.value = true
        _loadingStatus.value = "Nana is composing music with $model…"

        viewModelScope.launch {
            val sessionId = repository.ensureSessionExists(_currentSessionId.value)
            _currentSessionId.value = sessionId

            val result = repository.generateMusic(sessionId, prompt, model)
            _isLoading.value = false
            if (result is GeminiResult.Error) {
                _errorMessage.value = result.message
            }
        }
    }

    fun generateVideo(
        prompt: String,
        aspectRatio: String = "16:9"
    ) {
        if (prompt.isBlank() || _isLoading.value) return
        _errorMessage.value = null
        _isLoading.value = true
        _loadingStatus.value = "Nana is animating video with Veo 3.1 ($aspectRatio)…"

        viewModelScope.launch {
            val sessionId = repository.ensureSessionExists(_currentSessionId.value)
            _currentSessionId.value = sessionId

            val result = repository.generateVideo(sessionId, prompt, aspectRatio)
            _isLoading.value = false
            if (result is GeminiResult.Error) {
                _errorMessage.value = result.message
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioHelper.release()
    }
}
