package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We set dynamicColor = false so that our unique, gorgeous academic Red/Gold branding is consistently applied!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
