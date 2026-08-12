package com.rank.quickwallpaper.data.repository

import com.rank.quickwallpaper.data.local.FavoriteDao
import com.rank.quickwallpaper.data.local.FavoriteOfflineCache
import com.rank.quickwallpaper.data.local.HistoryDao
import com.rank.quickwallpaper.data.local.entities.FavoriteEntity
import com.rank.quickwallpaper.data.remote.PexelsApi
import com.rank.quickwallpaper.domain.model.Photo
import com.rank.quickwallpaper.domain.model.Photographer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoRepositoryTest {

    private val api = mockk<PexelsApi>()
    private val dao = mockk<FavoriteDao>(relaxed = true) {
        every { observeAll() } returns emptyFlow()
    }
    private val historyDao = mockk<HistoryDao>(relaxed = true)
    private val offlineCache = mockk<FavoriteOfflineCache>(relaxed = true)

    private fun repo() = PhotoRepository(api, dao, historyDao, offlineCache)

    private val samplePhoto = Photo(
        id = "99",
        width = 1080,
        height = 1920,
        description = "Sky",
        likes = 1,
        downloads = 2,
        thumbUrl = "https://example.com/t.jpg",
        smallUrl = "https://example.com/s.jpg",
        regularUrl = "https://example.com/r.jpg",
        fullUrl = "https://example.com/f.jpg",
        rawUrl = "https://example.com/raw.jpg",
        photographer = Photographer("7", "alice", "Alice", "https://example.com/p", null),
        color = "#112233",
        downloadLocation = "",
        htmlLink = "https://example.com/photo"
    )

    @Test
    fun `repository creates editorial feed without crash`() {
        val repository = repo()
        assertNotNull(repository.getEditorialFeed())
    }

    @Test
    fun `addFavorite maps photo fields into FavoriteEntity`() = runTest {
        val entitySlot = slot<FavoriteEntity>()
        coEvery { dao.insert(capture(entitySlot)) } returns Unit

        repo().addFavorite(samplePhoto)

        coVerify(exactly = 1) { dao.insert(any()) }
        val entity = entitySlot.captured
        assertEquals("99", entity.id)
        assertEquals("https://example.com/t.jpg", entity.thumbUrl)
        assertEquals("Alice", entity.photographerName)
        assertEquals("alice", entity.photographerUsername)
        assertEquals("#112233", entity.color)
    }

    @Test
    fun `isFavorite delegates to dao flow`() = runTest {
        every { dao.isFavorite("99") } returns flowOf(true)
        val fav = repo().isFavorite("99").first()
        assertTrue(fav)
    }

    @Test
    fun `removeFavorite deletes by id`() = runTest {
        repo().removeFavorite("99")
        coVerify { dao.deleteById("99") }
    }
}
