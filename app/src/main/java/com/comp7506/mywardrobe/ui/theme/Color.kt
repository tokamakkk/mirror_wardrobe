package com.comp7506.mywardrobe.ui.theme

import androidx.compose.ui.graphics.Color

// Modern soft mesh gradient palette
val MeshBeige = Color(0xFFFDFBF7)
val MeshLavender = Color(0xFFE6E6FA)
val MeshPeach = Color(0xFFFFF5EE)
val MeshMint = Color(0xFFF2FFF8)
val MeshSky = Color(0xFFF2F7FF)

// Semantic colors (light)
val SoftPrimary = Color(0xFF7E6EA9)
val SoftSecondary = Color(0xFFB38B6D)
val SoftBackground = MeshBeige
val SoftSurface = Color(0xCCFFFFFF)
val SoftSurfaceVariant = Color(0xB3FFFFFF)
val SoftText = Color(0xFF2F2A37)
val SoftSubtext = Color(0xFF6A6375)
val SoftDivider = Color(0x33FFFFFF)
val SoftError = Color(0xFFB5534A)

// Semantic colors (dark)
val DarkBackground = Color(0xFF15141B)
val DarkSurface = Color(0xCC24222D)
val DarkSurfaceVariant = Color(0xB3332F40)
val DarkPrimary = Color(0xFFC6B7F5)
val DarkSecondary = Color(0xFFE7C1A6)
val DarkOnSurface = Color(0xFFF2EEF9)
val DarkSubtext = Color(0xFFC8C2D6)

// Legacy aliases kept for any hardcoded usages in screens
@Deprecated("Use SoftBackground", ReplaceWith("SoftBackground"))
val AppBackground = SoftBackground

@Deprecated("Use SoftSurface", ReplaceWith("SoftSurface"))
val CardBackground = SoftSurface
