package com.rank.quickwallpaper.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rank.quickwallpaper.data.local.entities.FavoriteEntity
import com.rank.quickwallpaper.data.local.entities.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    suspend fun getAll(): List<FavoriteEntity>

    @Query("SELECT id FROM favorites")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :photoId)")
    fun isFavorite(photoId: String): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE id = :photoId LIMIT 1")
    suspend fun getById(photoId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("UPDATE favorites SET localCachedPath = :path WHERE id = :photoId")
    suspend fun updateLocalPath(photoId: String, path: String)

    @Query("DELETE FROM favorites WHERE id = :photoId")
    suspend fun deleteById(photoId: String)

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun count(): Int
}

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY viewedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 40): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HistoryEntity)

    @Query("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY viewedAt DESC LIMIT :keep)")
    suspend fun trim(keep: Int = 40)

    @Query("DELETE FROM history")
    suspend fun clear()
}
