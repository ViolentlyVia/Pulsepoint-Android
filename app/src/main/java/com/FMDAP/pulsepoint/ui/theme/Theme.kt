package com.FMDAP.pulsepoint.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary        = Cyan80,
    secondary      = Teal80,
    tertiary       = Teal40,
)

private val LightColors = lightColorScheme(
    primary        = Cyan40,
    secondary      = Teal40,
    tertiary       = Teal80,
)

@Composable
fun PulsePointTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = Typography, content = content)
}
