package com.comp7506.mywardrobe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SoftPrimary,
    onPrimary = Color.White,
    primaryContainer = MeshLavender,
    onPrimaryContainer = SoftText,
    secondary = SoftSecondary,
    onSecondary = Color.White,
    secondaryContainer = MeshPeach,
    onSecondaryContainer = SoftText,
    background = SoftBackground,
    onBackground = SoftText,
    surface = SoftSurface,
    onSurface = SoftText,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = SoftSubtext,
    outline = SoftDivider,
    error = SoftError,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkBackground,
    primaryContainer = Color(0xFF453A62),
    onPrimaryContainer = DarkOnSurface,
    secondary = DarkSecondary,
    onSecondary = DarkBackground,
    secondaryContainer = Color(0xFF51423B),
    onSecondaryContainer = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSubtext,
    outline = DarkSubtext,
    error = Color(0xFFCF6679),
    onError = Color(0xFF1C0009),
)

@Composable
fun MyWardrobeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
