package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PromptXoDarkColorScheme = darkColorScheme(
    primary = PromptXoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF261D42),
    onPrimaryContainer = Color(0xFFDDD6FE),
    secondary = PromptXoSecondary,
    onSecondary = Color.White,
    tertiary = PromptXoTertiary,
    background = PromptXoBackground,
    onBackground = PromptXoTextPrimary,
    surface = PromptXoSurface,
    onSurface = PromptXoTextPrimary,
    surfaceVariant = PromptXoSurfaceVariant,
    onSurfaceVariant = PromptXoTextSecondary,
    outline = PromptXoOutline,
    error = PromptXoError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PromptXoDarkColorScheme,
        typography = Typography,
        content = content
    )
}
