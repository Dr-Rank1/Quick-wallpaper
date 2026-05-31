package com.rank.quickwallpaper.ui.home

import androidx.paging.PagingData
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.domain.model.Photo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class HomeViewModelTest {

    private val repo = mockk<PhotoRepository> {
        every { getEditorialFeed() } returns flowOf(PagingData.empty<Photo>())
    }

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun `viewModel initializes with empty paging data`() = runTest {
        val vm = HomeViewModel(repo)
        val result = vm.photos.first()
        assertNotNull(result)
    }
}
