package com.rank.quickwallpaper.ui.home

import androidx.paging.PagingData
import com.rank.quickwallpaper.data.repository.PhotoRepository
import com.rank.quickwallpaper.domain.model.Photo
import com.rank.quickwallpaper.domain.model.Photographer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val samplePhoto = Photo(
        id = "42",
        width = 100,
        height = 200,
        description = null,
        likes = 0,
        downloads = 0,
        thumbUrl = "",
        smallUrl = "",
        regularUrl = "",
        fullUrl = "",
        rawUrl = "",
        photographer = Photographer("1", "u", "Name", "https://example.com", null),
        color = null,
        downloadLocation = "",
        htmlLink = ""
    )

    private val repo = mockk<PhotoRepository> {
        every { getEditorialFeed() } returns flowOf(PagingData.empty<Photo>())
        every { observeFavoriteIds() } returns flowOf(emptyList())
        every { observeHistory() } returns flowOf(emptyList())
        coEvery { getWallpaperOfTheDay() } returns Result.success(samplePhoto)
    }

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun `viewModel initializes with empty paging data`() = runTest {
        val vm = HomeViewModel(repo)
        assertNotNull(vm.photos.first())
    }

    @Test
    fun `shuffle emits photo id on success`() = runTest {
        coEvery { repo.getRandomPhoto() } returns Result.success(samplePhoto)
        val vm = HomeViewModel(repo)

        vm.shuffle()
        advanceUntilIdle()

        assertEquals("42", vm.randomPhotoId.value)
        vm.onRandomPhotoNavigated()
        assertNull(vm.randomPhotoId.value)
    }

    @Test
    fun `shuffle emits error on failure`() = runTest {
        coEvery { repo.getRandomPhoto() } returns Result.failure(IllegalStateException("No photos found"))
        val vm = HomeViewModel(repo)
        var error: String? = null
        val job = launch { error = vm.shuffleError.first() }

        vm.shuffle()
        advanceUntilIdle()
        job.join()

        assertEquals("No photos found", error)
        assertNull(vm.randomPhotoId.value)
    }
}
