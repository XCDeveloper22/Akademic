package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String, val isDark: Boolean) {
    DARK_MODE("Velvet Crimson", true),
    AKADEMIC_BLUE("Celestial Sapphire", true),
    FOREST_GREEN("Ethereal Emerald", true),
    SUNSET_ORANGE("Saffron Aurora", true),
    PURPLE_SCHOLAR("Mystic Amethyst", true),
    PINK_SCHOLAR("Plum Velvet", true),
    MIDNIGHT_BLACK("Stark Obsidian", true)
}

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkBg,
    primaryContainer = CrimsonPrimary,
    onPrimaryContainer = DarkOnBg,
    secondary = MetallicGold,
    onSecondary = DarkBg,
    tertiary = CrimsonPrimary,
    onTertiary = DarkOnBg,
    background = DarkBg,
    onBackground = DarkOnBg,
    surface = DarkSurface,
    onSurface = DarkOnBg,
    outline = GoldPrimary,
    error = RedAccent
)

private val LightColorScheme = lightColorScheme(
    primary = CrimsonPrimary,
    onPrimary = LightBg,
    primaryContainer = GoldPrimary,
    onPrimaryContainer = LightOnBg,
    secondary = MetallicGold,
    onSecondary = LightOnBg,
    tertiary = GoldLight,
    onTertiary = LightOnBg,
    background = LightBg,
    onBackground = LightOnBg,
    surface = LightSurface,
    onSurface = LightOnBg,
    outline = CrimsonPrimary,
    error = RedAccent
)

val AkademicBlueColorScheme = darkColorScheme(
    primary = Color(0xFF64D2FF), // Bright academic light blue
    onPrimary = Color(0xFF071426), // Very dark navy
    primaryContainer = Color(0xFF1E3A8A), // Royal Blue container
    onPrimaryContainer = Color(0xFFE0E6ED),
    secondary = Color(0xFF3B82F6), // Electric blue
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF0B132B), // Deep Midnight Navy Space Blue
    surface = Color(0xFF1C2541), // Deep Charcoal Navy
    onBackground = Color(0xFFE0E6ED),
    onSurface = Color(0xFFE0E6ED),
    outline = Color(0xFF3B82F6).copy(alpha = 0.5f),
    error = Color(0xFFFF4D4D)
)

val ForestGreenColorScheme = darkColorScheme(
    primary = Color(0xFF81C784), // Light forest green accent
    onPrimary = Color(0xFF091F0B), // Deep dark forest background
    primaryContainer = Color(0xFF1B5E20), // Strong green container
    onPrimaryContainer = Color(0xFFF1F8F1),
    secondary = Color(0xFF4CAF50),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF0D1F10), // Rich forest moss black
    surface = Color(0xFF163219), // Pine card surface
    onBackground = Color(0xFFF1F8F1),
    onSurface = Color(0xFFF1F8F1),
    outline = Color(0xFF4CAF50).copy(alpha = 0.5f),
    error = Color(0xFFFF4D4D)
)

val SunsetOrangeColorScheme = darkColorScheme(
    primary = Color(0xFFFFB74D), // Sunny harvest orange
    onPrimary = Color(0xFF230D05), // Burned coffee brown
    primaryContainer = Color(0xFFD84315), // Deep Rust Orange
    onPrimaryContainer = Color(0xFFFFF5F0),
    secondary = Color(0xFFF97316),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF1A0E0B), // Warm Charcoal Brown
    surface = Color(0xFF2D1914), // Autumn clay cards
    onBackground = Color(0xFFFFF5F0),
    onSurface = Color(0xFFFFF5F0),
    outline = Color(0xFFF97316).copy(alpha = 0.5f),
    error = Color(0xFFFF4D4D)
)

val PurpleScholarColorScheme = darkColorScheme(
    primary = Color(0xFFD6BCFA), // Lavender scholar primary
    onPrimary = Color(0xFF1F1135), // Royal purple violet
    primaryContainer = Color(0xFF6D28D9), // Vibrant violet container
    onPrimaryContainer = Color(0xFFF5F3FF),
    secondary = Color(0xFF8B5CF6),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF110A1C), // Deep midnight purple void
    surface = Color(0xFF1D1230), // Amethyst card board
    onBackground = Color(0xFFF5F3FF),
    onSurface = Color(0xFFF5F3FF),
    outline = Color(0xFF8B5CF6).copy(alpha = 0.5f),
    error = Color(0xFFFF4D4D)
)

val PinkBlossomColorScheme = lightColorScheme(
    primary = Color(0xFFDB2777), // Deep blush pink
    onPrimary = Color(0xFFFAF5F7),
    primaryContainer = Color(0xFFF472B6), // Light blossom container
    onPrimaryContainer = Color(0xFF371B26),
    secondary = Color(0xFFEC4899),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFAF5F7), // Pale cherry blossom clay
    surface = Color(0xFFFFFFFF), // Crisp clean card surface
    onBackground = Color(0xFF371B26),
    onSurface = Color(0xFF371B26),
    outline = Color(0xFFDB2777).copy(alpha = 0.3f),
    error = Color(0xFFFF4D4D)
)

val PinkScholarColorScheme = darkColorScheme(
    primary = Color(0xFFF472B6), // Radiant blossom pink
    onPrimary = Color(0xFF260515), // Dark cherry background depth
    primaryContainer = Color(0xFF9D174D), // Bold raspberry
    onPrimaryContainer = Color(0xFFFFF0F5),
    secondary = Color(0xFFDB2777),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF1F1116), // Dark plum velvet canvas
    surface = Color(0xFF2D1921), // Cozy pink mahogany cards
    onBackground = Color(0xFFFFF0F5),
    onSurface = Color(0xFFFFF0F5),
    outline = Color(0xFFDB2777).copy(alpha = 0.5f),
    error = Color(0xFFFF4D4D)
)

val MidnightBlackColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF), // Premium stark white
    onPrimary = Color(0xFF000000), // Vantablack void
    primaryContainer = Color(0xFF222222), // Deep iron outline
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF8E8E93), // Monochromatic silver
    onSecondary = Color(0xFF1C1C1E),
    background = Color(0xFF000000), // True deep AMOLED black
    surface = Color(0xFF121212), // Dark metal slab cards
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    outline = Color(0xFFFFFFFF).copy(alpha = 0.3f),
    error = Color(0xFFFF4D4D)
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.DARK_MODE,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.DARK_MODE -> DarkColorScheme
        AppTheme.AKADEMIC_BLUE -> AkademicBlueColorScheme
        AppTheme.FOREST_GREEN -> ForestGreenColorScheme
        AppTheme.SUNSET_ORANGE -> SunsetOrangeColorScheme
        AppTheme.PURPLE_SCHOLAR -> PurpleScholarColorScheme
        AppTheme.PINK_SCHOLAR -> PinkScholarColorScheme
        AppTheme.MIDNIGHT_BLACK -> MidnightBlackColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
