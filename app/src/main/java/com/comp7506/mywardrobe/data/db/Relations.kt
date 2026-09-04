package com.comp7506.mywardrobe.data.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class OutfitWithItems(
    @Embedded val outfit: OutfitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = OutfitItemCrossRefEntity::class,
            parentColumn = "outfitId",
            entityColumn = "itemId",
        ),
    )
    val items: List<ClothingItemEntity>,
)

data class CategoryCount(
    val category: String,
    val count: Int,
)

data class ItemWearCount(
    val itemId: Long,
    val name: String,
    val wearCount: Int,
)

