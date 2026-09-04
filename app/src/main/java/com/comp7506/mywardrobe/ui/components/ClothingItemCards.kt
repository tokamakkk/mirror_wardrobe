package com.comp7506.mywardrobe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.comp7506.mywardrobe.data.db.ClothingItemEntity

@Composable
fun ClothingItemGridCard(
    item: ClothingItemEntity,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF)),
    ) {
        Box {
            Column {
                if (item.imageUri.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(8.dp)
                            .background(Color(0x40FFFFFF), RoundedCornerShape(16.dp)),
                    )
                } else {
                    AsyncImage(
                        model = item.imageUri,
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(8.dp)
                            .background(Color(0x40FFFFFF), RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit,
                    )
                }
                Text(
                    text = item.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color(0x99FFFFFF), RoundedCornerShape(12.dp)),
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFF1C1B1F),
                    )
                }
            }
        }
    }
}

@Composable
fun ClothingItemListRow(
    item: ClothingItemEntity,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (item.imageUri.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0x40FFFFFF), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp)),
                )
            } else {
                AsyncImage(
                    model = item.imageUri,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0x40FFFFFF), RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.background(Color(0x99FFFFFF), RoundedCornerShape(12.dp)),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFF1C1B1F))
                }
            }
        }
    }
}

