package com.comp7506.mywardrobe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    contentPadding: Dp = 18.dp,
    shadowAlpha: Float = 0.05f,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(modifier = modifier) {
        // Diffuse floating shadow layer.
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(56.dp)
                .background(Color.Black.copy(alpha = shadowAlpha), shape)
                .graphicsLayer {
                    shadowElevation = 56.dp.toPx()
                    ambientShadowColor = Color.Black.copy(alpha = shadowAlpha)
                    spotShadowColor = Color.Black.copy(alpha = shadowAlpha)
                    this.shape = shape
                    clip = false
                },
        )

        // Frosted glass body background.
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    this.shape = shape
                    clip = true
                }
                .clip(shape)
                .background(Color(0xCCFFFFFF)),
        )

        // Content layer. Keep it wrap-content to avoid over-expanding
        // cards that do not have explicit height constraints.
        Box(
            modifier = Modifier.padding(contentPadding),
            content = content,
        )
    }
}
