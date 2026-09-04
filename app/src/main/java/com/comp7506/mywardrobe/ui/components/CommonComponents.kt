package com.comp7506.mywardrobe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.comp7506.mywardrobe.data.db.ClothingItemEntity

@Composable
fun OutfitTileLayout(
    items: List<ClothingItemEntity>,
    modifier: Modifier = Modifier,
    maxItems: Int = 9,
    gap: Dp = 6.dp,
    itemCornerRadius: Dp = 10.dp,
) {
    val displayItems = items.take(maxItems)
    val count = displayItems.size
    if (count == 0) {
        Box(modifier = modifier.background(Color(0x80FFFFFF)))
        return
    }

    val columns = when {
        count <= 1 -> 1
        count <= 2 -> 2
        count <= 4 -> 2
        else -> 3
    }
    val rows = ((count + columns - 1) / columns).coerceAtLeast(1)

    BoxWithConstraints(
        modifier = modifier.background(Color(0x80FFFFFF)),
    ) {
        val cellWidth = (maxWidth - gap * (columns - 1)).coerceAtLeast(0.dp) / columns
        val cellHeight = (maxHeight - gap * (rows - 1)).coerceAtLeast(0.dp) / rows

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            repeat(rows) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    repeat(columns) { colIndex ->
                        val itemIndex = rowIndex * columns + colIndex
                        if (itemIndex < count) {
                            val item = displayItems[itemIndex]
                            Box(
                                modifier = Modifier
                                    .width(cellWidth)
                                    .height(cellHeight)
                                    .clip(RoundedCornerShape(itemCornerRadius))
                                    .background(Color.White.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (item.imageUri.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFE9EEF5)),
                                    )
                                } else {
                                    SubcomposeAsyncImage(
                                        model = item.imageUri,
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp),
                                        contentScale = ContentScale.Fit,
                                        loading = {
                                            CircularProgressIndicator(
                                                modifier = Modifier.width(18.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xAA6E657F),
                                            )
                                        },
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.width(cellWidth).height(cellHeight))
                        }
                    }
                }
            }
        }
    }
}
