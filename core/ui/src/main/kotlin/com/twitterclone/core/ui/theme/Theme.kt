package com.twitterclone.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val XBlue = Color(0xFF1D9BF0)
val XPink = Color(0xFFF91880)
val XGreen = Color(0xFF00BA7C)
val XBlack = Color(0xFF000000)
val XText = Color(0xFFE7E9EA)
val XGray = Color(0xFF71767B)
val XHairline = Color(0xFF2F3336)
val XSurface = Color(0xFF16181C)

private val XDarkColors =
    darkColorScheme(
        primary = XBlue,
        onPrimary = Color.White,
        primaryContainer = XBlue,
        onPrimaryContainer = Color.White,
        secondary = XGray,
        onSecondary = Color.White,
        background = XBlack,
        onBackground = XText,
        surface = XBlack,
        onSurface = XText,
        surfaceVariant = XSurface,
        onSurfaceVariant = XGray,
        surfaceContainer = XBlack,
        surfaceContainerHigh = XSurface,
        outline = XHairline,
        outlineVariant = XHairline,
        error = Color(0xFFF4212E),
        onError = Color.White,
    )

private val XTypography =
    Typography(
        headlineMedium =
            TextStyle(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
            ),
        headlineSmall =
            TextStyle(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                lineHeight = 30.sp,
            ),
        titleLarge =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 24.sp,
            ),
        titleMedium =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 22.sp,
            ),
        titleSmall =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            ),
        bodyLarge =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 22.sp,
            ),
        bodyMedium =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            ),
        bodySmall =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),
        labelLarge =
            TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            ),
        labelMedium =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 16.sp,
            ),
        labelSmall =
            TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 16.sp,
            ),
    )

private val XShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

@Composable
fun TwitterCloneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            @Suppress("DEPRECATION")
            view.systemUiVisibility =
                view.systemUiVisibility and
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    .inv() and
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    .inv()
        }
    }
    MaterialTheme(
        colorScheme = XDarkColors,
        typography = XTypography,
        shapes = XShapes,
        content = content,
    )
}
