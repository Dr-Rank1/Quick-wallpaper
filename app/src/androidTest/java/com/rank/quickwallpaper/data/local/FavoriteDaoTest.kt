package com.rank.quickwallpaper.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rank.quickwallpaper.data.local.entities.FavoriteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    private lateinit var db: PaziaDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PaziaDatabase::class.java
        ).build()
        dao = db.favoriteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(
        id: String,
        photographerName: String = "Test",
        color: String? = "#FF0000"
    ) = FavoriteEntity(
        id = id,
        thumbUrl = "https://example.com/thumb.jpg",
        regularUrl = "https://example.com/reg.jpg",
        fullUrl = "https://example.com/full.jpg",
        photographerName = photographerName,
        photographerUsername = "testuser",
        photographerProfileUrl = "https://example.com/profile",
        width = 1920,
        height = 1080,
        color = color,
        downloadLocation = "https://example.com/download"
    )

    @Test
    fun insertAndObserve() = runBlocking {
        dao.insert(entity("test1"))
        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("test1", all[0].id)
    }

    @Test
    fun deleteById() = runBlocking {
        dao.insert(entity("delete_me", photographerName = "", color = null))
        dao.deleteById("delete_me")
        val all = dao.observeAll().first()
        assertEquals(0, all.size)
    }

    @Test
    fun isFavorite_returnsTrueWhenExists() = runBlocking {
        dao.insert(entity("fav1", photographerName = "", color = null))
        val fav = dao.isFavorite("fav1").first()
        assertEquals(true, fav)
    }
}
