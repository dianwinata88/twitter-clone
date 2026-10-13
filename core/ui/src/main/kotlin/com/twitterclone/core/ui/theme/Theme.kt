package com.twitterclone.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** X accent blue used for primary actions, links, and active states. */
val XBlue = Color(0xFF1D9BF0)

/** X like pink used for the liked state. */
val XPink = Color(0xFFF91880)

/**
 * X Dark ("lights out") palette: pure-black background, white/gray text,
 * blue accents. The clone is dark-only, matching X's dark theme.
 */
private val XDarkColors =
    darkColorScheme(
        primary = XBlue,
        onPrimary = Color.White,
        primaryContainer = XBlue,
        onPrimaryContainer = Color.White,
        secondary = Color(0xFF71767B),
        onSecondary = Color.White,
        background = Color(0xFF000000),
        onBackground = Color(0xFFE7E9EA),
        surface = Color(0xFF000000),
        onSurface = Color(0xFFE7E9EA),
        surfaceVariant = Color(0xFF16181C),
        onSurfaceVariant = Color(0xFF71767B),
        outline = Color(0xFF2F3336),
        error = Color(0xFFF4212E),
        onError = Color.White,
    )

@Composable
fun TwitterCloneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = XDarkColors,
        content = content,
    )
}
