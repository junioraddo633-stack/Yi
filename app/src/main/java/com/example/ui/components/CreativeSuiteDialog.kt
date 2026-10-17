package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.NanaAiBubble
import com.example.ui.theme.NanaBlack
import com.example.ui.theme.NanaBlueAccent
import com.example.ui.theme.NanaBluePrimary
import com.example.ui.theme.NanaBorder
import com.example.ui.theme.NanaCyanGlow
import com.example.ui.theme.NanaDarkBg
import com.example.ui.theme.NanaSurface
import com.example.ui.theme.NanaSurfaceElevated
import com.example.ui.theme.NanaSurfaceVariant
import com.example.ui.theme.NanaTextPrimary
import com.example.ui.theme.NanaTextSecondary

@Composable
fun CreativeSuiteDialog(
    isThinkingMode: Boolean,
    isSearchGrounding: Boolean,
    isMapsGrounding: Boolean,
    onToggleThinking: (Boolean) -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    onToggleMaps: (Boolean) -> Unit,
    onGenerateImage: (prompt: String, model: String, size: String, ratio: String) -> Unit,
    onGenerateMusic: (prompt: String, model: String) -> Unit,
    onGenerateVideo: (prompt: String, ratio: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Modes & Tools", "Image Studio", "Music Lab", "Video Studio")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("creative_suite_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = NanaSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NanaBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NanaCyanGlow)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nana's Creative Studio",
                            color = NanaTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = NanaTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = NanaDarkBg,
                    contentColor = NanaCyanGlow,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NanaCyanGlow
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = if (selectedTab == index) NanaCyanGlow else NanaTextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> ModesTab(
                            isThinkingMode = isThinkingMode,
                            isSearchGrounding = isSearchGrounding,
                            isMapsGrounding = isMapsGrounding,
                            onToggleThinking = onToggleThinking,
                            onToggleSearch = onToggleSearch,
                            onToggleMaps = onToggleMaps
                        )
                        1 -> ImageStudioTab(onGenerateImage = { p, m, s, r ->
                            onGenerateImage(p, m, s, r)
                            onDismiss()
                        })
                        2 -> MusicStudioTab(onGenerateMusic = { p, m ->
                            onGenerateMusic(p, m)
                            onDismiss()
                        })
                        3 -> VideoStudioTab(onGenerateVideo = { p, r ->
                            onGenerateVideo(p, r)
                            onDismiss()
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun ModesTab(
    isThinkingMode: Boolean,
    isSearchGrounding: Boolean,
    isMapsGrounding: Boolean,
    onToggleThinking: (Boolean) -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    onToggleMaps: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // High Thinking Card
        ToolToggleCard(
            icon = Icons.Default.Psychology,
            iconTint = NanaCyanGlow,
            title = "High Thinking Mode",
            subtitle = "Powers Nana with Gemini 4 Argon & maximum reasoning depth for solving complex coding, math, and analytical puzzles.",
            checked = isThinkingMode,
            onCheckedChange = onToggleThinking
        )

        // Google Search Grounding Card
        ToolToggleCard(
            icon = Icons.Default.Language,
            iconTint = NanaBluePrimary,
            title = "Google Search Grounding",
            subtitle = "Grounds Nana's knowledge in real-time Google web search data for up-to-date facts, current news, and live info.",
            checked = isSearchGrounding,
            onCheckedChange = onToggleSearch
        )

        // Google Maps Grounding Card
        ToolToggleCard(
            icon = Icons.Default.Map,
            iconTint = Color(0xFF34D399),
            title = "Google Maps Grounding",
            subtitle = "Equips Nana with Google Maps location intelligence for accurate directions, local venues, and geographical details.",
            checked = isMapsGrounding,
            onCheckedChange = onToggleMaps
        )
    }
}

@Composable
private fun ToolToggleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = NanaSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (checked) iconTint.copy(alpha = 0.5f) else NanaBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = NanaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, color = NanaTextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NanaBlack,
                    checkedTrackColor = iconTint,
                    uncheckedThumbColor = NanaTextSecondary,
                    uncheckedTrackColor = NanaSurfaceElevated
                )
            )
        }
    }
}

@Composable
private fun ImageStudioTab(
    onGenerateImage: (prompt: String, model: String, size: String, ratio: String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("gemini-3-pro-image-preview") }
    var selectedSize by remember { mutableStateOf("1K") }
    var selectedRatio by remember { mutableStateOf("1:1") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Describe your image:", color = NanaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            placeholder = { Text("e.g. A friendly glowing blue robot exploring an alien bioluminescent garden", color = NanaTextSecondary, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NanaTextPrimary,
                unfocusedTextColor = NanaTextPrimary,
                focusedBorderColor = NanaCyanGlow,
                unfocusedBorderColor = NanaBorder
            )
        )

        // Resolution selector
        Text("Resolution:", color = NanaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1K", "2K", "4K").forEach { size ->
                FilterChip(
                    selected = selectedSize == size,
                    onClick = { selectedSize = size },
                    label = { Text(size, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NanaCyanGlow,
                        selectedLabelColor = NanaBlack
                    )
                )
            }
        }

        // Aspect ratio selector
        Text("Aspect Ratio:", color = NanaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1:1", "16:9", "9:16").forEach { ratio ->
                FilterChip(
                    selected = selectedRatio == ratio,
                    onClick = { selectedRatio = ratio },
                    label = { Text(ratio, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NanaBluePrimary,
                        selectedLabelColor = NanaBlack
                    )
                )
            }
        }

        // Model selector
        Text("Model:", color = NanaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "gemini-3-pro-image-preview" to "Gemini 3 Pro Image",
                "gemini-nano-banana-2.1" to "Nano Banana 2.1"
            ).forEach { (m, label) ->
                FilterChip(
                    selected = selectedModel == m,
                    onClick = { selectedModel = m },
                    label = { Text(label, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NanaBlueAccent,
                        selectedLabelColor = NanaTextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { onGenerateImage(prompt, selectedModel, selectedSize, selectedRatio) },
            enabled = prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NanaCyanGlow, contentColor = NanaBlack)
        ) {
            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Generate Image with Nana", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MusicStudioTab(
    onGenerateMusic: (prompt: String, model: String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf("lyria-3-clip-preview") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Music Theme & Style:", color = NanaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            placeholder = { Text("e.g. Chill lo-fi ambient beats with soft electric piano and gentle rain", color = NanaTextSecondary, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NanaTextPrimary,
                unfocusedTextColor = NanaTextPrimary,
                focusedBorderColor = NanaCyanGlow,
                unfocusedBorderColor = NanaBorder
            )
        )

        Text("Model Track Length:", color = NanaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "lyria-3-clip-preview" to "Lyria Clip (Up to 30s)",
                "lyria-3-pro-preview" to "Lyria Pro (Full Track)"
            ).forEach { (m, label) ->
                FilterChip(
                    selected = selectedModel == m,
                    onClick = { selectedModel = m },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NanaCyanGlow,
                        selectedLabelColor = NanaBlack
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = { onGenerateMusic(prompt, selectedModel) },
            enabled = prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NanaBluePrimary, contentColor = NanaBlack)
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Compose Music with Lyria", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VideoStudioTab(
    onGenerateVideo: (prompt: String, ratio: String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var selectedRatio by remember { mutableStateOf("16:9") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Video Prompt (Veo 3.1 Fast):", color = NanaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            placeholder = { Text("e.g. A holographic neon dolphin swimming across the night sky above Tokyo", color = NanaTextSecondary, fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NanaTextPrimary,
                unfocusedTextColor = NanaTextPrimary,
                focusedBorderColor = NanaCyanGlow,
                unfocusedBorderColor = NanaBorder
            )
        )

        Text("Aspect Ratio:", color = NanaTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "16:9" to "Landscape (16:9)",
                "9:16" to "Portrait (9:16)"
            ).forEach { (r, label) ->
                FilterChip(
                    selected = selectedRatio == r,
                    onClick = { selectedRatio = r },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NanaBlueAccent,
                        selectedLabelColor = NanaTextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = { onGenerateVideo(prompt, selectedRatio) },
            enabled = prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NanaCyanGlow, contentColor = NanaBlack)
        ) {
            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Animate Video with Veo", fontWeight = FontWeight.Bold)
        }
    }
}
