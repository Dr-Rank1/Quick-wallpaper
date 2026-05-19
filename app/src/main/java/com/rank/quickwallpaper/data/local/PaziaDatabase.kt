package com.rank.quickwallpaper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rank.quickwallpaper.data.local.entities.FavoriteEntity

@Database(
    entities = [FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PaziaDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}
