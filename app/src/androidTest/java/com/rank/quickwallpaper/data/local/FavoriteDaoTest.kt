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

    @Test
    fun insertAndObserve() = runBlocking {
        val entity = FavoriteEntity(
            id = "test1",
            smallUrl = "https://example.com/small.jpg",
            regularUrl = "https://example.com/reg.jpg",
            fullUrl = "https://example.com/full.jpg",
            htmlLink = "https://example.com",
            photographerName = "Test",
            photographerProfileUrl = "https://example.com/profile",
            color = "#FF0000",
            width = 1920,
            height = 1080,
            description = "A test photo"
        )
        dao.insert(entity)
        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("test1", all[0].id)
    }

    @Test
    fun deleteById() = runBlocking {
        val entity = FavoriteEntity(
            id = "delete_me", smallUrl = "", regularUrl = "",
            fullUrl = "", htmlLink = "", photographerName = "",
            photographerProfileUrl = "", color = null,
            width = 0, height = 0, description = null
        )
        dao.insert(entity)
        dao.deleteById("delete_me")
        val all = dao.observeAll().first()
        assertEquals(0, all.size)
    }

    @Test
    fun isFavorite_returnsTrueWhenExists() = runBlocking {
        val entity = FavoriteEntity(
            id = "fav1", smallUrl = "", regularUrl = "",
            fullUrl = "", htmlLink = "", photographerName = "",
            photographerProfileUrl = "", color = null,
            width = 0, height = 0, description = null
        )
        dao.insert(entity)
        val fav = dao.isFavorite("fav1")
        assertEquals(true, fav)
    }
}
