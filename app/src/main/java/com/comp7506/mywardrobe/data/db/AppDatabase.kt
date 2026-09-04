package com.comp7506.mywardrobe.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    entities = [
        ClothingItemEntity::class,
        OutfitEntity::class,
        OutfitItemCrossRefEntity::class,
        OutfitRecordEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao
    abstract fun outfitDao(): OutfitDao
    abstract fun outfitRecordDao(): OutfitRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS users")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS users")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE outfits ADD COLUMN occasion TEXT")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN warmthValue INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE outfit_item_cross_ref ADD COLUMN offsetX REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE outfit_item_cross_ref ADD COLUMN offsetY REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE outfit_item_cross_ref ADD COLUMN scale REAL NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE outfit_item_cross_ref ADD COLUMN rotation REAL NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mywardrobe.db",
                ).addMigrations(MIGRATION_1_3, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build().also { INSTANCE = it }
            }
        }
    }
}

