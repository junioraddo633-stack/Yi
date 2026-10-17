package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.ui.theme.NanaAiBubble
import com.example.ui.theme.NanaBlack
import com.example.ui.theme.NanaBlueAccent
import com.example.ui.theme.NanaBlueDark
import com.example.ui.theme.NanaBluePrimary
import com.example.ui.theme.NanaBorder
import com.example.ui.theme.NanaBorderHighlight
import com.example.ui.theme.NanaCyanGlow
import com.example.ui.theme.NanaDarkBg
import com.example.ui.theme.NanaError
import com.example.ui.theme.NanaErrorContainer
import com.example.ui.theme.NanaSurface
import com.example.ui.theme.NanaSurfaceElevated
import com.example.ui.theme.NanaSurfaceVariant
import com.example.ui.theme.NanaTextPrimary
import com.example.ui.theme.NanaTextSecondary
import com.example.ui.theme.NanaUserBubble
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NanaTopAppBar(
    currentModel: String,
    isThinkingMode: Boolean,
    isSearchGrounding: Boolean,
    isMapsGrounding: Boolean,
    isAutoSpeak: Boolean,
    onModelSelected: (String) -> Unit,
    onToggleAutoSpeak: () -> Unit,
    onNewChatClicked: () -> Unit,
    onOpenHistoryClicked: () -> Unit,
    onOpenCreativeSuite: () -> Unit,
    modifier: Modifier = Modifier
) {
    var modelMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NanaSurface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Avatar & Branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenHistoryClicked() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, NanaCyanGlow, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.nana_avatar),
                            contentDescription = "Nana AI Avatar",
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Nana AI",
                                color = NanaTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NanaCyanGlow)
                            )
                        }
                        Text(
                            text = "Friendly & Curious Companion",
                            color = NanaCyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Right Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Creative tools
                    IconButton(
                        onClick = onOpenCreativeSuite,
                        modifier = Modifier.testTag("creative_suite_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Creative Suite",
                            tint = NanaCyanGlow
                        )
                    }

                    // TTS Voice Toggle
                    IconButton(
                        onClick = onToggleAutoSpeak,
                        modifier = Modifier.testTag("auto_speak_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Auto read-aloud",
                            tint = if (isAutoSpeak) NanaCyanGlow else NanaTextSecondary
                        )
                    }

                    // History Button
                    IconButton(
                        onClick = onOpenHistoryClicked,
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Conversation History",
                            tint = NanaTextPrimary
                        )
                    }

                    // New Chat
                    IconButton(
                        onClick = onNewChatClicked,
                        modifier = Modifier.testTag("new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Start New Chat",
                            tint = NanaBluePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-row: Active Model Pill & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Model dropdown selector pill
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { modelMenuExpanded = true }
                            .border(1.dp, NanaBorder, RoundedCornerShape(14.dp)),
                        color = NanaSurfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = when (currentModel) {
                                    "gemini-4-argon" -> Icons.Default.Psychology
                                    "gemini-3.1-flash-lite" -> Icons.Default.Refresh
                                    else -> Icons.Default.AutoAwesome
                                },
                                contentDescription = null,
                                tint = NanaBluePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentModel) {
                                    "gemini-4-argon" -> "Gemini 4 Argon (Thinker)"
                                    "gemini-3.1-flash-lite" -> "Gemini 3.1 Flash-Lite (Fast)"
                                    else -> "Gemini 3.8 Flash (Smart)"
                                },
                                color = NanaTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = modelMenuExpanded,
                        onDismissRequest = { modelMenuExpanded = false },
                        modifier = Modifier.background(NanaSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Gemini 3.8 Flash (Recommended)", color = NanaBluePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Balanced, clever & fast for general chat", color = NanaTextSecondary, fontSize = 11.sp)
                                }
                            },
                            onClick = {
                                onModelSelected("gemini-3.8-flash")
                                modelMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Gemini 4 Argon (High Thinking)", color = NanaCyanGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Deep reasoning & advanced problem solving", color = NanaTextSecondary, fontSize = 11.sp)
                                }
                            },
                            onClick = {
                                onModelSelected("gemini-4-argon")
                                modelMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text("Gemini 3.1 Flash-Lite (Low Latency)", color = NanaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Instant lightning quick responses", color = NanaTextSecondary, fontSize = 11.sp)
                                }
                            },
                            onClick = {
                                onModelSelected("gemini-3.1-flash-lite")
                                modelMenuExpanded = false
                            }
                        )
                    }
                }

                if (isThinkingMode) {
                    FeatureBadge(text = "🧠 High Thinking", color = NanaCyanGlow)
                }
                if (isSearchGrounding) {
                    FeatureBadge(text = "🌐 Search Grounding", color = NanaBluePrimary)
                }
                if (isMapsGrounding) {
                    FeatureBadge(text = "🗺️ Maps Grounding", color = Color(0xFF34D399))
                }
            }
        }
    }
}

@Composable
fun FeatureBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    isSpeaking: Boolean,
    onSpeak: (String) -> Unit,
    onStopSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.role.lowercase() == "user"
    var showFullImageDialog by remember { mutableStateOf(false) }

    val decodedBitmap = remember(message.mediaData) {
        if (message.mediaType == "image" && !message.mediaData.isNullOrBlank()) {
            try {
                val bytes = Base64.decode(message.mediaData, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Nana Avatar Icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.dp, NanaCyanGlow.copy(alpha = 0.7f), CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nana_avatar),
                    contentDescription = "Nana AI",
                    modifier = Modifier.size(34.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            // Bubble Card
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = when {
                    message.isError -> NanaErrorContainer
                    isUser -> NanaUserBubble
                    else -> NanaAiBubble
                },
                border = if (!isUser && !message.isError) {
                    androidx.compose.foundation.BorderStroke(1.dp, NanaBorder)
                } else if (message.isError) {
                    androidx.compose.foundation.BorderStroke(1.dp, NanaError.copy(alpha = 0.5f))
                } else null,
                shadowElevation = 2.dp,
                modifier = Modifier.testTag(if (isUser) "user_message_bubble" else "nana_message_bubble")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header tag if special mode was used
                    if (!isUser) {
                        if (message.isThinking) {
                            Text(
                                text = "🧠 High Thinking Mode",
                                color = NanaCyanGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        } else if (message.isGrounded) {
                            Text(
                                text = "🌐 Live Grounding Verified",
                                color = NanaBluePrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    // Text Content
                    Text(
                        text = message.content,
                        color = if (message.isError) NanaError else NanaTextPrimary,
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp
                    )

                    // Image display if attached
                    if (decodedBitmap != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            bitmap = decodedBitmap.asImageBitmap(),
                            contentDescription = "Generated by Nana AI",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, NanaCyanGlow.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable { showFullImageDialog = true }
                        )
                    }

                    // Audio visual banner
                    if (message.mediaType == "audio") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = NanaSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NanaBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = NanaCyanGlow)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Synthesized Music Track", color = NanaTextPrimary, fontSize = 12.sp)
                            }
                        }
                    }

                    // Video visual banner
                    if (message.mediaType == "video") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = NanaSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NanaBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = NanaBluePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Veo Video Animation Track", color = NanaTextPrimary, fontSize = 12.sp)
                            }
                        }
                    }

                    // Message Footer & Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timestamp
                        Text(
                            text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                            color = NanaTextSecondary,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Copy button
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Nana AI Message", message.content)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = NanaTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Speak button for Nana's responses
                        if (!isUser && !message.isError) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    if (isSpeaking) onStopSpeak() else onSpeak(message.content)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isSpeaking) "Stop voice" else "Read aloud",
                                    tint = if (isSpeaking) NanaCyanGlow else NanaTextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full screen image preview dialog
    if (showFullImageDialog && decodedBitmap != null) {
        Dialog(onDismissRequest = { showFullImageDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NanaSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nana AI Generated Image", color = NanaTextPrimary, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showFullImageDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = NanaTextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(
                        bitmap = decodedBitmap.asImageBitmap(),
                        contentDescription = "Full Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun TypingIndicator(
    loadingText: String = "Nana is thinking…",
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha1 by transition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "dot1"
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse), label = "dot2"
    )
    val alpha3 by transition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse), label = "dot3"
    )

    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("typing_indicator"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(1.dp, NanaCyanGlow, CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.nana_avatar),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NanaAiBubble,
            border = androidx.compose.foundation.BorderStroke(1.dp, NanaCyanGlow.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bouncing Dots
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(NanaCyanGlow.copy(alpha = alpha1)))
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(NanaCyanGlow.copy(alpha = alpha2)))
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(NanaCyanGlow.copy(alpha = alpha3)))

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = loadingText,
                    color = NanaCyanGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ErrorBanner(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("error_banner"),
        shape = RoundedCornerShape(12.dp),
        color = NanaErrorContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, NanaError.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nana encountered an issue",
                    color = NanaError,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = errorMessage,
                    color = NanaTextPrimary,
                    fontSize = 12.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = NanaCyanGlow)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = NanaTextSecondary)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmptyChatHero(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero Avatar
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .border(2.5.dp, Brush.sweepGradient(listOf(NanaCyanGlow, NanaBluePrimary, NanaBlueAccent, NanaCyanGlow)), CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.nana_avatar),
                contentDescription = "Nana AI",
                modifier = Modifier.size(110.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hi, I'm Nana AI!",
            color = NanaTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your friendly, clever & curious assistant. Ask me anything, explore deep questions, or let's create something wonderful together!",
            color = NanaTextSecondary,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Try asking Nana:",
            color = NanaCyanGlow,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        val promptSuggestions = listOf(
            "Explain quantum computing like I'm 5 🌌",
            "Help me write a clever poem about space exploration 🚀",
            "What are 5 mind-bending facts about the ocean? 🐙",
            "Debug an asynchronous coroutine bug in Kotlin 💻",
            "Plan a 3-day weekend itinerary for Tokyo 🗼",
            "Paint a futuristic cyberpunk garden at night 🎨"
        )

        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            promptSuggestions.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NanaSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NanaBorder),
                    modifier = Modifier
                        .clickable { onPromptSelected(prompt) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = prompt,
                        color = NanaTextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onOpenCreativeSuite: () -> Unit,
    onMicClicked: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NanaSurface,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plus / Tools button
            IconButton(
                onClick = onOpenCreativeSuite,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NanaSurfaceVariant)
                    .testTag("tools_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Creative Tools",
                    tint = NanaCyanGlow,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Input Field
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    Text("Ask Nana anything…", color = NanaTextSecondary, fontSize = 14.sp)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("message_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NanaTextPrimary,
                    unfocusedTextColor = NanaTextPrimary,
                    focusedBorderColor = NanaCyanGlow,
                    unfocusedBorderColor = NanaBorder,
                    focusedContainerColor = NanaDarkBg,
                    unfocusedContainerColor = NanaDarkBg,
                    cursorColor = NanaCyanGlow
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button or Voice
            if (text.isNotBlank()) {
                IconButton(
                    onClick = onSendClicked,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(listOf(NanaBluePrimary, NanaBlueAccent))
                        )
                        .testTag("send_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = NanaBlack,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = NanaBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = onMicClicked,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NanaSurfaceVariant)
                        .testTag("voice_input_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice input",
                        tint = NanaBluePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
