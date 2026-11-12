package com.goldtracker.india.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GoldPrimary = Color(0xFFB8860B)
val GoldBright = Color(0xFFD4AF37)
val GoldSoft = Color(0xFFFFF4D2)
val ObsidianBg = Color(0xFF121212)
val ObsidianSurface = Color(0xFF1E1D1A)
val IvoryBg = Color(0xFFFAF8F3)
val WarmCard = Color(0xFFFFFFFF)
val EmeraldUp = Color(0xFF1B8A5A)
val CrimsonDown = Color(0xFFC62828)

private val LightColors = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.White,
    primaryContainer = GoldSoft,
    onPrimaryContainer = Color(0xFF3A2800),
    secondary = Color(0xFF5D4B24),
    background = IvoryBg,
    surface = WarmCard,
    surfaceVariant = Color(0xFFF3EFE4),
    onBackground = Color(0xFF1C1B17),
    onSurface = Color(0xFF1C1B17)
)

private val DarkColors = darkColorScheme(
    primary = GoldBright,
    onPrimary = Color(0xFF281D00),
    primaryContainer = Color(0xFF473500),
    onPrimaryContainer = GoldSoft,
    secondary = Color(0xFFD8C593),
    background = ObsidianBg,
    surface = ObsidianSurface,
    surfaceVariant = Color(0xFF2A2822),
    onBackground = Color(0xFFF4F0E8),
    onSurface = Color(0xFFF4F0E8)
)

@Composable
fun GoldTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
