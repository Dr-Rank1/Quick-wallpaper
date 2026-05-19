package com.rank.quickwallpaper.data.remote

import com.rank.quickwallpaper.data.remote.dto.CollectionMediaResponseDto
import com.rank.quickwallpaper.data.remote.dto.FeaturedCollectionsResponseDto
import com.rank.quickwallpaper.data.remote.dto.PhotoDto
import com.rank.quickwallpaper.data.remote.dto.PhotoListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PexelsApi {

    @GET("v1/curated")
    suspend fun getCuratedPhotos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): PhotoListResponseDto

    @GET("v1/search")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): PhotoListResponseDto

    @GET("v1/photos/{id}")
    suspend fun getPhoto(@Path("id") id: Long): PhotoDto

    @GET("v1/collections/featured")
    suspend fun getFeaturedCollections(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): FeaturedCollectionsResponseDto

    @GET("v1/collections/{id}")
    suspend fun getCollectionMedia(
        @Path("id") id: String,
        @Query("type") type: String = "photos",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): CollectionMediaResponseDto
}
