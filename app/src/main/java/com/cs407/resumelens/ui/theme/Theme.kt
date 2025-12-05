package com.cs407.resumelens.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(

    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenPrimaryContainer,
    onPrimaryContainer = GreenPrimary,

    secondary = GreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = GreenSecondaryContainer,
    onSecondaryContainer = GreenSecondary,

    tertiary = GreenTertiary,
    onTertiary = Color.White,

    background = GreenBackground,
    onBackground = TextPrimary,

    surface = GreenSurface,
    onSurface = TextPrimary,

    surfaceVariant = GreenSecondaryContainer,
    onSurfaceVariant = TextSecondary,

    outline = GreenOutline,

    error = ErrorRed,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    // TODO: later we design true dark mode
    primary = GreenPrimaryContainer,
    onPrimary = Color.Black,
    background = Color(0xFF0B0F0D),
    onBackground = Color.White
)

@Composable
fun ResumeLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
