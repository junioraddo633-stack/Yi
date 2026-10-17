package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NanaDarkColorScheme = darkColorScheme(
    primary = NanaBluePrimary,
    onPrimary = NanaBlack,
    primaryContainer = NanaBlueAccent,
    onPrimaryContainer = NanaTextPrimary,
    secondary = NanaBlueSecondary,
    onSecondary = NanaBlack,
    secondaryContainer = NanaSurfaceVariant,
    onSecondaryContainer = NanaTextPrimary,
    tertiary = NanaCyanGlow,
    onTertiary = NanaBlack,
    background = NanaDarkBg,
    onBackground = NanaTextPrimary,
    surface = NanaSurface,
    onSurface = NanaTextPrimary,
    surfaceVariant = NanaSurfaceVariant,
    onSurfaceVariant = NanaTextSecondary,
    surfaceContainer = NanaSurfaceElevated,
    outline = NanaBorder,
    outlineVariant = NanaBorderHighlight,
    error = NanaError,
    onError = NanaBlack,
    errorContainer = NanaErrorContainer,
    onErrorContainer = NanaError
)

@Composable
fun NanaAiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NanaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
