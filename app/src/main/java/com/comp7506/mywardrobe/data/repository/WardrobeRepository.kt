package com.comp7506.mywardrobe.data.repository

import com.comp7506.mywardrobe.data.db.AppDatabase
import com.comp7506.mywardrobe.data.db.ClothingItemEntity
import com.comp7506.mywardrobe.data.db.OutfitEntity
import com.comp7506.mywardrobe.data.db.OutfitItemCrossRefEntity
import com.comp7506.mywardrobe.data.db.OutfitRecordEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class WardrobeRepository(private val db: AppDatabase) {
    data class OutfitItemTransform(
        val offsetX: Float = 0f,
        val offsetY: Float = 0f,
        val scale: Float = 1f,
        val rotation: Float = 0f,
    )
    fun observeClothingItems(category: String?, query: String): Flow<List<ClothingItemEntity>> {
        return db.clothingDao().observeFiltered(category = category, query = query)
    }

    fun observeTotalClothingCount(): Flow<Int> = db.clothingDao().observeTotalCount()

    fun observeCategoryCounts() = db.clothingDao().observeCategoryCounts()

    suspend fun addClothingItem(
        name: String,
        category: String,
        warmthValue: Int,
        imageUri: String?,
    ): Long {
        return db.clothingDao().insert(
            ClothingItemEntity(
                name = name,
                category = category,
                warmthValue = warmthValue,
                imageUri = imageUri,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteClothingItem(id: Long) {
        db.clothingDao().deleteClothingItem(id)
    }

    fun observeOutfits() = db.outfitDao().observeOutfitsWithItems()

    fun observeRecentOutfits(limit: Int) = db.outfitDao().observeRecentOutfitsWithItems(limit)

    /**
     * 根据ID列表获取衣物详情
     */
    suspend fun getClothingItemsByIds(ids: List<Long>): List<ClothingItemEntity> {
        return ids.mapNotNull { id ->
            db.clothingDao().getById(id)
        }
    }

    suspend fun addOutfit(
        name: String,
        occasion: String?,
        itemIdsInOrder: List<Long>,
        imageUri: String?,
    ): Long {
        val outfitId = db.outfitDao().insertOutfit(
            OutfitEntity(
                name = name,
                occasion = occasion,
                imageUri = imageUri,
                createdAt = System.currentTimeMillis(),
            ),
        )
        val refs = itemIdsInOrder.mapIndexed { index, itemId ->
            OutfitItemCrossRefEntity(outfitId = outfitId, itemId = itemId, position = index)
        }
        if (refs.isNotEmpty()) {
            db.outfitDao().insertCrossRefs(refs)
        }
        return outfitId
    }

    suspend fun getOrderedItemRefsForOutfit(outfitId: Long): List<OutfitItemCrossRefEntity> {
        return db.outfitDao().getOrderedItemRefsForOutfit(outfitId)
    }

    suspend fun updateOutfit(
        outfitId: Long,
        name: String,
        itemIdsInOrder: List<Long>,
        imageUri: String?,
        occasion: String? = null,
        itemTransforms: Map<Long, OutfitItemTransform> = emptyMap(),
    ) {
        db.withTransaction {
            val existing = db.outfitDao().getOutfitById(outfitId) ?: return@withTransaction
            val trimmedName = name.trim().ifBlank { existing.name }
            db.outfitDao().updateOutfit(
                existing.copy(
                    name = trimmedName,
                    occasion = occasion ?: existing.occasion,
                    imageUri = imageUri ?: existing.imageUri,
                ),
            )
            db.outfitDao().deleteCrossRefsForOutfit(outfitId)
            val refs = itemIdsInOrder.mapIndexed { index, itemId ->
                val transform = itemTransforms[itemId] ?: OutfitItemTransform()
                OutfitItemCrossRefEntity(
                    outfitId = outfitId,
                    itemId = itemId,
                    position = index,
                    offsetX = transform.offsetX,
                    offsetY = transform.offsetY,
                    scale = transform.scale,
                    rotation = transform.rotation,
                )
            }
            if (refs.isNotEmpty()) {
                db.outfitDao().insertCrossRefs(refs)
            }
        }
    }

    suspend fun addOutfitFromPhoto(imageUri: String): Long {
        return addOutfit(
            name = "Photo outfit",
            occasion = null,
            itemIdsInOrder = emptyList(),
            imageUri = imageUri,
        )
    }

    suspend fun deleteOutfit(outfitId: Long) {
        db.withTransaction {
            db.outfitRecordDao().deleteByOutfitId(outfitId)
            db.outfitDao().deleteCrossRefsForOutfit(outfitId)
            db.outfitDao().deleteOutfitById(outfitId)
        }
    }

    suspend fun setOutfitRecord(date: String, outfitId: Long) {
        val existing = db.outfitRecordDao().getByDate(date)
        db.outfitRecordDao().upsert(
            OutfitRecordEntity(
                id = existing?.id ?: 0,
                date = date,
                outfitId = outfitId,
            ),
        )
    }

    suspend fun deleteOutfitRecord(date: String) {
        db.outfitRecordDao().deleteByDate(date)
    }

    fun observeOutfitRecordsBetween(start: String, end: String) = db.outfitRecordDao().observeBetween(start, end)

    fun observeItemWearCounts(limit: Int) = db.outfitRecordDao().observeItemWearCounts(limit)

    /**
     * 获取单个衣物详情
     */
    suspend fun getItemById(id: Long): ClothingItemEntity? {
        return db.clothingDao().getById(id)
    }
}
