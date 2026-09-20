package com.example.ui.components

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatGPTTypingBox(
    promptText: String,
    toolName: String,
    toolLink: String,
    onGeneratedStateChanged: (Boolean) -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    var isGenerating by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var currentText by remember { mutableStateOf("") }
    var userHasEdited by remember { mutableStateOf(false) }

    val internalScrollState = rememberScrollState()

    // Streaming typing animation loop
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            currentText = ""
            userHasEdited = false
            val words = promptText.split(" ")
            val sb = StringBuilder()

            for (i in words.indices) {
                if (userHasEdited) {
                    // User typed manually: append smoothly without wiping user edits
                    sb.clear()
                    sb.append(currentText)
                    if (!currentText.endsWith(" ")) sb.append(" ")
                    sb.append(words[i])
                    currentText = sb.toString()
                } else {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append(words[i])
                    currentText = sb.toString()
                }

                // Auto-scroll internally inside the box so text scrolls up inside the box
                // while the outer page stays completely still and the hero image is never pushed up!
                try {
                    internalScrollState.scrollTo(internalScrollState.maxValue)
                } catch (_: Exception) {}

                delay(45)
            }
            isGenerating = false
            isCompleted = true
            onGeneratedStateChanged(true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141728))
            .border(1.dp, Color(0xFF282D4A), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("chatgpt_typing_box")
    ) {
        // "Generate Prompt" button (visible before generation)
        if (!isGenerating && !isCompleted) {
            Button(
                onClick = { isGenerating = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_prompt_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate Prompt",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Specific Bounded Prompt Box
        if (isGenerating || isCompleted) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isGenerating) Color(0xFF10B981) else Color(0xFF818CF8))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGenerating) "Generating Prompt..." else "Generated Prompt",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isCompleted) {
                    Text(
                        text = "Editable",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SPECIFIC BOUNDED BOX (Fixed height 140dp) with internal vertical scroll
            // User requirement: text stays inside this specific box, auto-scrolls internally, outer page doesn't scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C0E1A))
                    .border(1.dp, Color(0xFF232842), RoundedCornerShape(12.dp))
                    .verticalScroll(internalScrollState)
                    .padding(14.dp)
            ) {
                BasicTextField(
                    value = currentText,
                    onValueChange = { newText ->
                        userHasEdited = true
                        currentText = newText
                    },
                    textStyle = TextStyle(
                        color = Color(0xFFF1F5F9),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        fontFamily = FontFamily.Default
                    ),
                    cursorBrush = SolidColor(Color(0xFFA78BFA)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_text_field")
                )

                if (isGenerating && currentText.isEmpty()) {
                    Text(
                        text = "Typing prompt...",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                }
            }

            // USER INSTRUCTION: "Copy Prompt" and "Try in [tool]" buttons MUST ONLY SHOW AFTER GENERATION COMPLETES!
            if (isCompleted) {
                Spacer(modifier = Modifier.height(14.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Copy Prompt Button
                    Button(
                        onClick = {
                            val copyAction = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText("Prompt", currentText)
                                clipboard?.setPrimaryClip(clip)

                                coroutineScope.launch {
                                    snackbarHostState?.showSnackbar("Prompt copied!")
                                }
                            }

                            if (activity != null) {
                                AdManager.showRewardedAd(activity) {
                                    copyAction()
                                }
                            } else {
                                copyAction()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("copy_prompt_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2238))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Prompt",
                            tint = Color(0xFFC4B5FD),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Copy Prompt",
                            color = Color(0xFFE2E8F0),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (toolLink.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(toolLink))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("try_in_tool_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF7C3AED), Color(0xFF6366F1))
                                )
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC4B5FD))
                        ) {
                            Text(
                                text = if (toolName.isNotBlank()) toolName else "Try in ChatGPT ->",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
