package com.comp7506.mywardrobe.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object UiTokens {
    val appGradientTop = Color(0xFFE6E1F9)
    val appGradientBottom = Color(0xFFF3E7D9)
    val appGradientBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(appGradientTop, appGradientBottom),
        )

    val screenHorizontalPadding = 24.dp
    val screenVerticalPadding = 18.dp

    val wardrobeGridSpacing = 14.dp
    val wardrobeBottomContentPadding = 92.dp

    val homeChartColors = listOf(
        Color(0xFFA5B4FC), // Tops
        Color(0xFFFDBA8C), // Bottoms
        Color(0xFFE9D5A3), // Shoes
        Color(0xFFA8D5CB), // Accessories
        Color(0xFFCDB4DB), // Others
        Color(0xFFD7B8A8), // Extras
    )
    val homeRecentCard = Color(0xFFF3ECDD)

    val navBarContainer = Color(0xD9FFFFFF)
    val navSelected = Color(0xFF1C1B1F)
    val navUnselected = Color(0xFF4A4A4A)
    val navMirrorGlow = Color(0x66FFFFFF)
}
