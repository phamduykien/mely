package com.mely.sleep.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    secondary = IndigoLight,
    onSecondary = Color.White,
    background = NightBg,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary
)

@Composable
fun MelySleepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
