package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.ChatScreen
import com.example.ui.theme.NanaAiTheme
import com.example.ui.theme.NanaDarkBg
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NanaAiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NanaDarkBg
                ) {
                    ChatScreen(viewModel = chatViewModel)
                }
            }
        }
    }
}
