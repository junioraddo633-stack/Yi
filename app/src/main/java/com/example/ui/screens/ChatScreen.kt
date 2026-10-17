package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.CreativeSuiteDialog
import com.example.ui.components.EmptyChatHero
import com.example.ui.components.ErrorBanner
import com.example.ui.components.NanaTopAppBar
import com.example.ui.components.SessionsDrawerContent
import com.example.ui.components.TypingIndicator
import com.example.ui.theme.NanaDarkBg
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sessions by viewModel.sessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingStatus by viewModel.loadingStatus.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val selectedModel by viewModel.selectedModel.collectAsState()
    val isThinkingMode by viewModel.isThinkingMode.collectAsState()
    val isSearchGrounding by viewModel.isSearchGrounding.collectAsState()
    val isMapsGrounding by viewModel.isMapsGrounding.collectAsState()
    val isAutoSpeak by viewModel.isAutoSpeakEnabled.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var isCreativeSuiteOpen by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val listState = rememberLazyListState()

    // Speech-to-Text recognizer launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = if (inputText.isBlank()) spokenText else "$inputText $spokenText"
            }
        }
    }

    // Auto-scroll to bottom when new messages arrive or loading begins
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Handle back button for drawer or dialog
    BackHandler(enabled = drawerState.isOpen || isCreativeSuiteOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (isCreativeSuiteOpen) {
            isCreativeSuiteOpen = false
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(NanaDarkBg)) {
        val isTablet = maxWidth >= 700.dp

        if (isTablet) {
            // Adaptive Tablet Layout: Side-by-side Sessions rail + Chat Pane
            Row(modifier = Modifier.fillMaxSize()) {
                SessionsDrawerContent(
                    sessions = sessions,
                    currentSessionId = currentSessionId,
                    onSessionSelected = { viewModel.selectSession(it) },
                    onNewChatClicked = { viewModel.createNewSession() },
                    onDeleteSession = { viewModel.deleteSession(it) },
                    onCloseDrawer = {},
                    modifier = Modifier.fillMaxHeight()
                )

                ChatMainContent(
                    viewModel = viewModel,
                    messages = messages,
                    isLoading = isLoading,
                    loadingStatus = loadingStatus,
                    errorMessage = errorMessage,
                    selectedModel = selectedModel,
                    isThinkingMode = isThinkingMode,
                    isSearchGrounding = isSearchGrounding,
                    isMapsGrounding = isMapsGrounding,
                    isAutoSpeak = isAutoSpeak,
                    isSpeaking = isSpeaking,
                    inputText = inputText,
                    onInputTextChanged = { inputText = it },
                    onSendClicked = {
                        val text = inputText
                        inputText = ""
                        viewModel.sendMessage(text)
                    },
                    onOpenDrawer = {},
                    onOpenCreativeSuite = { isCreativeSuiteOpen = true },
                    onMicClicked = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Nana AI…")
                            }
                            speechRecognizerLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice recognition not available on this device", Toast.LENGTH_SHORT).show()
                        }
                    },
                    listState = listState,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Mobile Phone Layout with Modal Navigation Drawer
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    SessionsDrawerContent(
                        sessions = sessions,
                        currentSessionId = currentSessionId,
                        onSessionSelected = { viewModel.selectSession(it) },
                        onNewChatClicked = { viewModel.createNewSession() },
                        onDeleteSession = { viewModel.deleteSession(it) },
                        onCloseDrawer = { scope.launch { drawerState.close() } }
                    )
                }
            ) {
                ChatMainContent(
                    viewModel = viewModel,
                    messages = messages,
                    isLoading = isLoading,
                    loadingStatus = loadingStatus,
                    errorMessage = errorMessage,
                    selectedModel = selectedModel,
                    isThinkingMode = isThinkingMode,
                    isSearchGrounding = isSearchGrounding,
                    isMapsGrounding = isMapsGrounding,
                    isAutoSpeak = isAutoSpeak,
                    isSpeaking = isSpeaking,
                    inputText = inputText,
                    onInputTextChanged = { inputText = it },
                    onSendClicked = {
                        val text = inputText
                        inputText = ""
                        viewModel.sendMessage(text)
                    },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onOpenCreativeSuite = { isCreativeSuiteOpen = true },
                    onMicClicked = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Nana AI…")
                            }
                            speechRecognizerLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice recognition not available on this device", Toast.LENGTH_SHORT).show()
                        }
                    },
                    listState = listState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Creative Suite Dialog
        if (isCreativeSuiteOpen) {
            CreativeSuiteDialog(
                isThinkingMode = isThinkingMode,
                isSearchGrounding = isSearchGrounding,
                isMapsGrounding = isMapsGrounding,
                onToggleThinking = { viewModel.toggleThinkingMode(it) },
                onToggleSearch = { viewModel.toggleSearchGrounding(it) },
                onToggleMaps = { viewModel.toggleMapsGrounding(it) },
                onGenerateImage = { p, m, s, r -> viewModel.generateImage(p, m, s, r) },
                onGenerateMusic = { p, m -> viewModel.generateMusic(p, m) },
                onGenerateVideo = { p, r -> viewModel.generateVideo(p, r) },
                onDismiss = { isCreativeSuiteOpen = false }
            )
        }
    }
}

@Composable
private fun ChatMainContent(
    viewModel: ChatViewModel,
    messages: List<com.example.data.model.ChatMessage>,
    isLoading: Boolean,
    loadingStatus: String,
    errorMessage: String?,
    selectedModel: String,
    isThinkingMode: Boolean,
    isSearchGrounding: Boolean,
    isMapsGrounding: Boolean,
    isAutoSpeak: Boolean,
    isSpeaking: Boolean,
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenCreativeSuite: () -> Unit,
    onMicClicked: () -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        containerColor = NanaDarkBg,
        topBar = {
            NanaTopAppBar(
                currentModel = selectedModel,
                isThinkingMode = isThinkingMode,
                isSearchGrounding = isSearchGrounding,
                isMapsGrounding = isMapsGrounding,
                isAutoSpeak = isAutoSpeak,
                onModelSelected = { viewModel.selectModel(it) },
                onToggleAutoSpeak = { viewModel.toggleAutoSpeak() },
                onNewChatClicked = { viewModel.createNewSession() },
                onOpenHistoryClicked = onOpenDrawer,
                onOpenCreativeSuite = onOpenCreativeSuite
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChanged = onInputTextChanged,
                onSendClicked = onSendClicked,
                onOpenCreativeSuite = onOpenCreativeSuite,
                onMicClicked = onMicClicked,
                isLoading = isLoading
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Error Banner
            if (errorMessage != null) {
                ErrorBanner(
                    errorMessage = errorMessage,
                    onRetry = { viewModel.retryLastMessage() },
                    onDismiss = { /* auto cleared */ }
                )
            }

            // Message list or Empty State
            if (messages.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyChatHero(
                        onPromptSelected = { prompt ->
                            viewModel.sendMessage(prompt)
                        }
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatMessageItem(
                            message = message,
                            isSpeaking = isSpeaking,
                            onSpeak = { viewModel.speak(it) },
                            onStopSpeak = { viewModel.stopSpeaking() }
                        )
                    }

                    if (isLoading) {
                        item {
                            TypingIndicator(loadingText = loadingStatus)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}
