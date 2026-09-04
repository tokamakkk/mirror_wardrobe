package com.comp7506.mywardrobe.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clothing_items",
    indices = [
        Index(value = ["category"]),
        Index(value = ["createdAt"]),
    ],
)
data class ClothingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val warmthValue: Int,
    val imageUri: String?,
    val createdAt: Long,
)

@Entity(
    tableName = "outfits",
    indices = [
        Index(value = ["createdAt"]),
    ],
)
data class OutfitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val occasion: String?,
    val imageUri: String?,
    val createdAt: Long,
)

@Entity(
    tableName = "outfit_item_cross_ref",
    primaryKeys = ["outfitId", "itemId"],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["outfitId"]),
    ],
)
data class OutfitItemCrossRefEntity(
    val outfitId: Long,
    val itemId: Long,
    val position: Int,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
)

@Entity(
    tableName = "outfit_records",
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["outfitId"]),
    ],
)
data class OutfitRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val outfitId: Long,
)

