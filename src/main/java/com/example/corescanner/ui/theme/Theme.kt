package com.example.corescanner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = DarkBackground,

    secondary = PurpleGrey80,
    onSecondary = DarkTextPrimary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkTextPrimary,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    tertiary = Pink80,
    onTertiary = DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,

    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE7F6),
    onSecondaryContainer = Color(0xFF1C1130),

    background = Color(0xFFF5F2FF),
    onBackground = Color(0xFF1C1130),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1130),
    surfaceVariant = Color(0xFFEDE7F6),
    onSurfaceVariant = Color(0xFF4A4458),

    tertiary = Pink40,
    onTertiary = Color.White
)

@Composable
fun CoreScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Si quieres que SIEMPRE use tu paleta y no los colores dinámicos de Android 12+,
    // simplemente fija dynamicColor = false.
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