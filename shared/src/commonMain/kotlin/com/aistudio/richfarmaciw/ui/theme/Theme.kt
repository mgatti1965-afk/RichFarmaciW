package com.aistudio.richfarmaciw.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    secondary = Slate600,
    tertiary = GreenLightBg,
    background = Slate900,
    surface = Slate900,
    onPrimary = White,
    onSecondary = White,
    onTertiary = Slate900,
    onBackground = White,
    onSurface = White,
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = Slate600,
    tertiary = GreenLightBg,
    background = GrayBackground,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = GreenPrimary,
    onBackground = Slate900,
    onSurface = Slate900,
)

@Composable
fun RichFarmaciTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
