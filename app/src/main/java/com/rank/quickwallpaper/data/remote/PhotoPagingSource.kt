package com.rank.quickwallpaper.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.rank.quickwallpaper.data.remote.dto.toDomain
import com.rank.quickwallpaper.domain.model.Photo
import retrofit2.HttpException
import java.io.IOException

sealed class PexelsQuery {
    object Editorial : PexelsQuery()
    data class Search(val query: String) : PexelsQuery()
    data class Collection(val id: String) : PexelsQuery()
}

class PhotoPagingSource(
    private val api: PexelsApi,
    private val query: PexelsQuery
) : PagingSource<Int, Photo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val page = params.key ?: 1
        return try {
            val (photos, hasNext) = when (query) {
                is PexelsQuery.Editorial -> {
                    val r = api.getCuratedPhotos(page, params.loadSize)
                    r.photos.map { it.toDomain() } to (r.nextPage != null)
                }
                is PexelsQuery.Search -> {
                    val r = api.searchPhotos(query.query, page, params.loadSize)
                    r.photos.map { it.toDomain() } to (r.nextPage != null)
                }
                is PexelsQuery.Collection -> {
                    val r = api.getCollectionMedia(query.id, page = page, perPage = params.loadSize)
                    r.media.map { it.toDomain() } to (r.nextPage != null)
                }
            }
            LoadResult.Page(
                data = photos,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (hasNext) page + 1 else null
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
}
