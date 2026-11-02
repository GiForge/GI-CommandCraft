package com.example.totalcommander.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object CommanderThemes {

    // 1. Classic Blue (Iconic Total Commander styling)
    val ClassicBlueLight = lightColorScheme(
        primary = Color(0xFF0D47A1), // Classic TC Navy
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD0E4FF),
        onPrimaryContainer = Color(0xFF001D36),
        secondary = Color(0xFF1976D2),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE3F2FD),
        onSecondaryContainer = Color(0xFF0D47A1),
        tertiary = Color(0xFFF57F17), // Selection Amber
        onTertiary = Color.Black,
        background = Color(0xFFF8F9FA),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE8EEF5),
        onSurfaceVariant = Color(0xFF44474F),
        outline = Color(0xFF74777F)
    )

    // 2. Modern Dark
    val ModernDark = darkColorScheme(
        primary = Color(0xFF448AFF),
        onPrimary = Color(0xFF00227B),
        primaryContainer = Color(0xFF0D47A1),
        onPrimaryContainer = Color(0xFFD0E4FF),
        secondary = Color(0xFF82B1FF),
        onSecondary = Color(0xFF001B3F),
        secondaryContainer = Color(0xFF1E2838),
        onSecondaryContainer = Color(0xFFB0C6E8),
        tertiary = Color(0xFFFFD54F),
        onTertiary = Color(0xFF3E2723),
        background = Color(0xFF121418),
        onBackground = Color(0xFFE1E2E5),
        surface = Color(0xFF181C24),
        onSurface = Color(0xFFE1E2E5),
        surfaceVariant = Color(0xFF222834),
        onSurfaceVariant = Color(0xFFC4C6D0),
        outline = Color(0xFF8E9099)
    )

    // 3. Retro Norton Commander (Navy + Cyan + Yellow)
    val NortonRetro = darkColorScheme(
        primary = Color(0xFF00E5FF), // Cyan
        onPrimary = Color(0xFF00202E),
        primaryContainer = Color(0xFF003865),
        onPrimaryContainer = Color(0xFF80D8FF),
        secondary = Color(0xFFFFEB3B), // Yellow selection
        onSecondary = Color(0xFF333000),
        secondaryContainer = Color(0xFF002244),
        onSecondaryContainer = Color(0xFF82B1FF),
        tertiary = Color(0xFFFF5252),
        onTertiary = Color.White,
        background = Color(0xFF001026),
        onBackground = Color(0xFFE0F7FA),
        surface = Color(0xFF001E3D),
        onSurface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFF002B54),
        onSurfaceVariant = Color(0xFFB2EBF2),
        outline = Color(0xFF00838F)
    )

    // 4. Cyberpunk (Neon Purple & Cyan)
    val Cyberpunk = darkColorScheme(
        primary = Color(0xFF00F5D4),
        onPrimary = Color(0xFF002621),
        primaryContainer = Color(0xFF7B2CBF),
        onPrimaryContainer = Color(0xFFE0AAFF),
        secondary = Color(0xFFFF007F),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF240046),
        onSecondaryContainer = Color(0xFFFF99C8),
        tertiary = Color(0xFFFFE600),
        onTertiary = Color.Black,
        background = Color(0xFF0B0914),
        onBackground = Color(0xFFF3EAFF),
        surface = Color(0xFF140F2D),
        onSurface = Color(0xFFF3EAFF),
        surfaceVariant = Color(0xFF1F1A3A),
        onSurfaceVariant = Color(0xFFD4C2FC),
        outline = Color(0xFF5A189A)
    )

    fun getColorScheme(preset: String): ColorScheme {
        return when (preset.lowercase()) {
            "modern_dark" -> ModernDark
            "norton_cyan" -> NortonRetro
            "cyberpunk" -> Cyberpunk
            else -> ClassicBlueLight
        }
    }
}
