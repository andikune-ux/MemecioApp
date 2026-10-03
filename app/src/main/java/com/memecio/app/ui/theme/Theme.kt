package com.memecio.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF381868),
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = NeonCyan,
    onSecondary = Color(0xFF002026),
    secondaryContainer = Color(0xFF004D5B),
    onSecondaryContainer = Color(0xFFB5F6FF),
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = AccentError,
    onError = Color.White
)

@Composable
fun MemecioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We always use the signature sleek dark theme for Memecio
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
