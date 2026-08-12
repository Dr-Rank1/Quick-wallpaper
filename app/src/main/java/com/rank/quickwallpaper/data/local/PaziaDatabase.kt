package com.rank.quickwallpaper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rank.quickwallpaper.data.local.entities.FavoriteEntity
import com.rank.quickwallpaper.data.local.entities.HistoryEntity

@Database(
    entities = [FavoriteEntity::class, HistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PaziaDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE favorites ADD COLUMN localCachedPath TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS history (
                        id TEXT NOT NULL PRIMARY KEY,
                        thumbUrl TEXT NOT NULL,
                        regularUrl TEXT NOT NULL,
                        fullUrl TEXT NOT NULL,
                        photographerName TEXT NOT NULL,
                        color TEXT,
                        width INTEGER NOT NULL,
                        height INTEGER NOT NULL,
                        viewedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
