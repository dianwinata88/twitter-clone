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
 * "Professional Light" — a clean corporate palette (Linear/LinkedIn polish).
 * The app always renders in light mode; [darkTheme] is accepted for API
 * stability but ignored so the look is identical regardless of the system
 * setting.
 */

private val ProBlue = Color(0xFF2563EB)
private val ProBlueContainer = Color(0xFFDBEAFE)
private val ProOnBlueContainer = Color(0xFF1E40AF)
private val Ink = Color(0xFF0F172A)
private val SlateMuted = Color(0xFF64748B)
private val SurfaceTintSubtle = Color(0xFFF7F9FB)
private val FieldFill = Color(0xFFF1F5F9)
private val Hairline = Color(0xFFE5E9F0)
private val OutlineSlate = Color(0xFFCBD5E1)

private val LightColors =
    lightColorScheme(
        primary = ProBlue,
        onPrimary = Color.White,
        primaryContainer = ProBlueContainer,
        onPrimaryContainer = ProOnBlueContainer,
        secondary = SlateMuted,
        onSecondary = Color.White,
        secondaryContainer = ProBlueContainer,
        onSecondaryContainer = ProBlue,
        background = Color.White,
        onBackground = Ink,
        surface = Color.White,
        onSurface = Ink,
        surfaceVariant = FieldFill,
        onSurfaceVariant = SlateMuted,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = SurfaceTintSubtle,
        surfaceContainer = SurfaceTintSubtle,
        outline = OutlineSlate,
        outlineVariant = Hairline,
    )

private val ProTypography =
    Typography(
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
        headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
        titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
    )

private val ProShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

@Composable
fun TwitterCloneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = ProTypography,
        shapes = ProShapes,
        content = content,
    )
}
