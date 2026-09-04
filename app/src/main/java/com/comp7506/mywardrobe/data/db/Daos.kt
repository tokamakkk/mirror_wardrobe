package com.comp7506.mywardrobe.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: ClothingItemEntity): Long

    @Query("SELECT * FROM clothing_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ClothingItemEntity?

    @Query(
        """
        SELECT * FROM clothing_items
        WHERE (:category IS NULL OR category = :category)
          AND (name LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
        """,
    )
    fun observeFiltered(category: String?, query: String): Flow<List<ClothingItemEntity>>

    @Query("SELECT COUNT(*) FROM clothing_items")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT category, COUNT(*) AS count FROM clothing_items GROUP BY category")
    fun observeCategoryCounts(): Flow<List<CategoryCount>>

    @Query("DELETE FROM outfit_item_cross_ref WHERE itemId = :itemId")
    suspend fun deleteCrossRefsForItem(itemId: Long)

    @Query("DELETE FROM clothing_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun deleteClothingItem(id: Long) {
        deleteCrossRefsForItem(id)
        deleteById(id)
    }
}

@Dao
interface OutfitDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOutfit(outfit: OutfitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<OutfitItemCrossRefEntity>)

    @Update
    suspend fun updateOutfit(outfit: OutfitEntity)

    @Query("SELECT * FROM outfits WHERE id = :id LIMIT 1")
    suspend fun getOutfitById(id: Long): OutfitEntity?

    @Query(
        """
        SELECT itemId FROM outfit_item_cross_ref
        WHERE outfitId = :outfitId
        ORDER BY position ASC
        """,
    )
    suspend fun getOrderedItemIdsForOutfit(outfitId: Long): List<Long>

    @Query(
        """
        SELECT * FROM outfit_item_cross_ref
        WHERE outfitId = :outfitId
        ORDER BY position ASC
        """,
    )
    suspend fun getOrderedItemRefsForOutfit(outfitId: Long): List<OutfitItemCrossRefEntity>

    @Transaction
    @Query("SELECT * FROM outfits ORDER BY createdAt DESC")
    fun observeOutfitsWithItems(): Flow<List<OutfitWithItems>>

    @Transaction
    @Query("SELECT * FROM outfits ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecentOutfitsWithItems(limit: Int): Flow<List<OutfitWithItems>>

    @Query("DELETE FROM outfit_item_cross_ref WHERE outfitId = :outfitId")
    suspend fun deleteCrossRefsForOutfit(outfitId: Long)

    @Query("DELETE FROM outfits WHERE id = :outfitId")
    suspend fun deleteOutfitById(outfitId: Long)
}

@Dao
interface OutfitRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: OutfitRecordEntity): Long

    @Query("SELECT * FROM outfit_records WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): OutfitRecordEntity?

    @Query("SELECT * FROM outfit_records WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun observeBetween(start: String, end: String): Flow<List<OutfitRecordEntity>>

    @Query("DELETE FROM outfit_records WHERE outfitId = :outfitId")
    suspend fun deleteByOutfitId(outfitId: Long)

    @Query("DELETE FROM outfit_records WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query(
        """
        SELECT c.id AS itemId, c.name AS name, COUNT(r.id) AS wearCount
        FROM clothing_items c
        JOIN outfit_item_cross_ref x ON x.itemId = c.id
        JOIN outfit_records r ON r.outfitId = x.outfitId
        GROUP BY c.id, c.name
        ORDER BY wearCount DESC
        LIMIT :limit
        """,
    )
    fun observeItemWearCounts(limit: Int): Flow<List<ItemWearCount>>
}
