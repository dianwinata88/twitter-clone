package com.twitterclone.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * "Expressive M3" direction: a light-only violet/indigo scheme with a warm
 * rose tertiary (like states), large corner radii, and a heavier expressive
 * type scale. Feed surfaces sit on a tinted background so white cards pop.
 */
private val ExpressiveLightColors =
    lightColorScheme(
        primary = Color(0xFF5B54D6),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE4DFFF),
        onPrimaryContainer = Color(0xFF17125A),
        inversePrimary = Color(0xFFC3C0FF),
        secondary = Color(0xFF5D5C72),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE2E0F9),
        onSecondaryContainer = Color(0xFF1A1A2C),
        tertiary = Color(0xFFB2436B),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD9E3),
        onTertiaryContainer = Color(0xFF3E001F),
        background = Color(0xFFF3F0FA),
        onBackground = Color(0xFF1C1B21),
        surface = Color(0xFFFDF8FF),
        onSurface = Color(0xFF1C1B21),
        surfaceVariant = Color(0xFFE5E0EC),
        onSurfaceVariant = Color(0xFF48464F),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF3F0FA),
        surfaceContainer = Color(0xFFEDEAF6),
        surfaceContainerHigh = Color(0xFFE7E4F1),
        surfaceContainerHighest = Color(0xFFE1DEEB),
        surfaceBright = Color(0xFFFDF8FF),
        surfaceDim = Color(0xFFDCD7E6),
        outline = Color(0xFF79767F),
        outlineVariant = Color(0xFFCAC5D4),
        error = Color(0xFFBA1A1A),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
    )

private val ExpressiveShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(28.dp),
        extraLarge = RoundedCornerShape(36.dp),
    )

private val ExpressiveTypography =
    Typography(
        headlineLarge =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.5).sp,
            ),
        headlineMedium =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.25).sp,
            ),
        headlineSmall =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
            ),
        titleLarge =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
            ),
        titleMedium =
            TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
            ),
        titleSmall =
            TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        bodyLarge =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
        bodyMedium =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
            ),
        bodySmall =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
            ),
        labelLarge =
            TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        labelMedium =
            TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
        labelSmall =
            TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
    )

@Composable
fun TwitterCloneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Expressive direction ships light-only; `darkTheme` is kept in the
    // signature for API stability but is intentionally ignored.
    MaterialTheme(
        colorScheme = ExpressiveLightColors,
        typography = ExpressiveTypography,
        shapes = ExpressiveShapes,
        content = content,
    )
}
