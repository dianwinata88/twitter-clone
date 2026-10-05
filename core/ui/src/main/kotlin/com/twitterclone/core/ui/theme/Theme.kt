package com.twitterclone.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TwitterBlue = Color(0xFF1D9BF0)
private val TwitterBlueDark = Color(0xFF1A8CD8)

private val LightColors =
    lightColorScheme(
        primary = TwitterBlue,
        onPrimary = Color.White,
        secondary = Color(0xFF536471),
        background = Color.White,
        surface = Color.White,
        onBackground = Color(0xFF0F1419),
        onSurface = Color(0xFF0F1419),
        surfaceVariant = Color(0xFFEFF3F4),
        onSurfaceVariant = Color(0xFF536471),
    )

private val DarkColors =
    darkColorScheme(
        primary = TwitterBlueDark,
        onPrimary = Color.White,
        secondary = Color(0xFF8B98A5),
        background = Color(0xFF15202B),
        surface = Color(0xFF15202B),
        onBackground = Color(0xFFE7E9EA),
        onSurface = Color(0xFFE7E9EA),
        surfaceVariant = Color(0xFF273340),
        onSurfaceVariant = Color(0xFF8B98A5),
    )

@Composable
fun TwitterCloneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
