package com.rank.quickwallpaper.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.rank.quickwallpaper.data.local.FavoriteDao
import com.rank.quickwallpaper.data.local.FavoriteOfflineCache
import com.rank.quickwallpaper.data.local.HistoryDao
import com.rank.quickwallpaper.data.local.entities.FavoriteEntity
import com.rank.quickwallpaper.data.local.entities.HistoryEntity
import com.rank.quickwallpaper.data.remote.PexelsApi
import com.rank.quickwallpaper.data.remote.PexelsQuery
import com.rank.quickwallpaper.data.remote.PhotoPagingSource
import com.rank.quickwallpaper.data.remote.dto.toDomain
import com.rank.quickwallpaper.domain.model.Collection
import com.rank.quickwallpaper.domain.model.Photo
import com.rank.quickwallpaper.domain.model.Photographer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val api: PexelsApi,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao,
    private val offlineCache: FavoriteOfflineCache
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

    fun searchPhotos(query: String, color: String? = null): Flow<PagingData<Photo>> = Pager(
        config = feedConfig,
        pagingSourceFactory = { PhotoPagingSource(api, PexelsQuery.Search(query, color)) }
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
        val randomPage = (1..20).random()
        val response = api.getCuratedPhotos(page = randomPage, perPage = 30)
        val photos = response.photos.map { it.toDomain() }
        if (photos.isNotEmpty()) {
            photos.random()
        } else {
            val fallback = api.getCuratedPhotos(page = 1, perPage = 30)
            val fallbackPhotos = fallback.photos.map { it.toDomain() }
            require(fallbackPhotos.isNotEmpty()) { "No photos found" }
            fallbackPhotos.random()
        }
    }

    suspend fun getPhoto(id: String): Result<Photo> = runCatching {
        val numericId = id.toLongOrNull() ?: throw IllegalArgumentException("Invalid photo ID: $id")
        api.getPhoto(numericId).toDomain()
    }

    suspend fun getSimilarPhotos(photo: Photo): Result<List<Photo>> = runCatching {
        val query = buildString {
            photo.description?.takeIf { it.isNotBlank() }?.let { append(it.take(40)); append(' ') }
            append(photo.photographer.name)
        }.trim().ifBlank { "wallpaper" }
        api.searchPhotos(query = query, page = 1, perPage = 12)
            .photos
            .map { it.toDomain() }
            .filter { it.id != photo.id }
            .take(10)
    }

    suspend fun getWallpaperOfTheDay(): Result<Photo> = runCatching {
        api.getCuratedPhotos(page = 1, perPage = 1).photos.first().toDomain()
    }

    suspend fun getCollections(page: Int = 1): Result<List<Collection>> = runCatching {
        val collections = api.getFeaturedCollections(page = page, perPage = 20).collections.map { it.toDomain() }
        coroutineScope {
            collections.map { collection ->
                async {
                    val cover = runCatching {
                        api.getCollectionMedia(collection.id, page = 1, perPage = 1)
                            .media
                            .firstOrNull()
                            ?.toDomain()
                    }.getOrNull()
                    collection.copy(
                        coverPhotoUrl = cover?.regularUrl ?: cover?.thumbUrl,
                        coverColor = cover?.color
                    )
                }
            }.awaitAll()
        }
    }

    fun observeFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.observeAll()

    fun observeFavoriteIds(): Flow<List<String>> = favoriteDao.observeIds()

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
                downloadLocation = "",
                localCachedPath = offlineCache.pathFor(photo.id).orEmpty()
            )
        )
        // Best-effort offline cache for favorites.
        val path = offlineCache.cachePhoto(photo.id, photo.fullUrl)
        if (!path.isNullOrBlank()) favoriteDao.updateLocalPath(photo.id, path)
    }

    suspend fun removeFavorite(photoId: String) {
        favoriteDao.deleteById(photoId)
        offlineCache.delete(photoId)
    }

    suspend fun toggleFavorite(photo: Photo): Boolean {
        val existing = favoriteDao.getById(photo.id)
        return if (existing != null) {
            removeFavorite(photo.id)
            false
        } else {
            addFavorite(photo)
            true
        }
    }

    fun observeHistory(): Flow<List<HistoryEntity>> = historyDao.observeRecent()

    suspend fun recordHistory(photo: Photo) {
        historyDao.upsert(
            HistoryEntity(
                id = photo.id,
                thumbUrl = photo.thumbUrl,
                regularUrl = photo.regularUrl,
                fullUrl = photo.fullUrl,
                photographerName = photo.photographer.name,
                color = photo.color,
                width = photo.width,
                height = photo.height
            )
        )
        historyDao.trim(40)
    }

    suspend fun clearHistory() = historyDao.clear()

    suspend fun getFavoriteEntities(): List<FavoriteEntity> = favoriteDao.getAll()

    fun favoriteToPhoto(entity: FavoriteEntity): Photo = Photo(
        id = entity.id,
        width = entity.width,
        height = entity.height,
        description = null,
        likes = 0,
        downloads = 0,
        thumbUrl = entity.thumbUrl,
        smallUrl = entity.regularUrl,
        regularUrl = entity.regularUrl,
        fullUrl = entity.fullUrl,
        rawUrl = entity.fullUrl,
        photographer = Photographer(
            id = "",
            username = entity.photographerUsername,
            name = entity.photographerName,
            profileUrl = entity.photographerProfileUrl,
            avatarUrl = null
        ),
        color = entity.color,
        downloadLocation = entity.downloadLocation,
        htmlLink = "https://www.pexels.com/photo/${entity.id}/"
    )

    fun historyToPhoto(entity: HistoryEntity): Photo = Photo(
        id = entity.id,
        width = entity.width,
        height = entity.height,
        description = null,
        likes = 0,
        downloads = 0,
        thumbUrl = entity.thumbUrl,
        smallUrl = entity.regularUrl,
        regularUrl = entity.regularUrl,
        fullUrl = entity.fullUrl,
        rawUrl = entity.fullUrl,
        photographer = Photographer(
            id = "",
            username = "",
            name = entity.photographerName,
            profileUrl = "",
            avatarUrl = null
        ),
        color = entity.color,
        downloadLocation = "",
        htmlLink = "https://www.pexels.com/photo/${entity.id}/"
    )
}
