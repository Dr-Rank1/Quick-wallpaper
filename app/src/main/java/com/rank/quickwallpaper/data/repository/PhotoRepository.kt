package com.rank.quickwallpaper.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rank.quickwallpaper.data.local.FavoriteDao
import com.rank.quickwallpaper.data.local.entities.FavoriteEntity
import com.rank.quickwallpaper.data.remote.PexelsApi
import com.rank.quickwallpaper.data.remote.PexelsQuery
import com.rank.quickwallpaper.data.remote.PhotoPagingSource
import com.rank.quickwallpaper.data.remote.dto.toDomain
import com.rank.quickwallpaper.domain.model.Collection
import com.rank.quickwallpaper.domain.model.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val api: PexelsApi,
    private val favoriteDao: FavoriteDao
) {

    private val feedConfig = PagingConfig(
        pageSize = 30,
        prefetchDistance = 10,
        enablePlaceholders = false,
        initialLoadSize = 60
    )

    fun getEditorialFeed(): Flow<PagingData<Photo>> = Pager(
        config = feedConfig,
        pagingSourceFactory = { PhotoPagingSource(api, PexelsQuery.Editorial) }
    ).flow

    fun searchPhotos(query: String): Flow<PagingData<Photo>> = Pager(
        config = feedConfig,
        pagingSourceFactory = { PhotoPagingSource(api, PexelsQuery.Search(query)) }
    ).flow

    fun getCategoryPhotos(query: String): Flow<PagingData<Photo>> = Pager(
        config = feedConfig,
        pagingSourceFactory = { PhotoPagingSource(api, PexelsQuery.Search(query)) }
    ).flow

    fun getCollectionPhotos(collectionId: String): Flow<PagingData<Photo>> = Pager(
        config = feedConfig,
        pagingSourceFactory = { PhotoPagingSource(api, PexelsQuery.Collection(collectionId)) }
    ).flow

    suspend fun getRandomPhoto(): Result<Photo> = runCatching {
        val randomPage = (1..50).random()
        val response = api.getCuratedPhotos(page = randomPage, perPage = 30)
        val photos = response.photos.map { it.toDomain() }
        require(photos.isNotEmpty()) { "No photos found" }
        photos.random()
    }

    suspend fun getPhoto(id: String): Result<Photo> = runCatching {
        api.getPhoto(id.toLong()).toDomain()
    }

    suspend fun getCollections(page: Int = 1): Result<List<Collection>> = runCatching {
        api.getFeaturedCollections(page = page, perPage = 20).collections.map { it.toDomain() }
    }

    fun observeFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.observeAll()

    fun isFavorite(photoId: String): Flow<Boolean> = favoriteDao.isFavorite(photoId)

    suspend fun addFavorite(photo: Photo) {
        favoriteDao.insert(
            FavoriteEntity(
                id = photo.id,
                thumbUrl = photo.thumbUrl,
                regularUrl = photo.regularUrl,
                fullUrl = photo.fullUrl,
                photographerName = photo.photographer.name,
                photographerUsername = photo.photographer.username,
                photographerProfileUrl = photo.photographer.profileUrl,
                width = photo.width,
                height = photo.height,
                color = photo.color,
                downloadLocation = ""
            )
        )
    }

    suspend fun removeFavorite(photoId: String) {
        favoriteDao.deleteById(photoId)
    }
}
