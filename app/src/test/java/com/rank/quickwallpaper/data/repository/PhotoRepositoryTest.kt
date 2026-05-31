package com.rank.quickwallpaper.data.repository

import com.rank.quickwallpaper.data.local.FavoriteDao
import com.rank.quickwallpaper.data.remote.PexelsApi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Test

class PhotoRepositoryTest {

    private val api = mockk<PexelsApi>()
    private val dao = mockk<FavoriteDao> {
        every { observeAll() } returns emptyFlow()
    }

    @Test
    fun `repository creates without crash`() {
        val repo = PhotoRepository(api, dao)
        assert(repo.getEditorialFeed() is kotlinx.coroutines.flow.Flow)
    }
}
