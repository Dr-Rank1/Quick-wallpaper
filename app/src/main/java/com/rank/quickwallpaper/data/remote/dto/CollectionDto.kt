package com.rank.quickwallpaper.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CollectionDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String?,
    @Json(name = "private") val isPrivate: Boolean = false,
    @Json(name = "media_count") val mediaCount: Int,
    @Json(name = "photos_count") val photosCount: Int
)

@JsonClass(generateAdapter = true)
data class FeaturedCollectionsResponseDto(
    @Json(name = "collections") val collections: List<CollectionDto>,
    @Json(name = "page") val page: Int,
    @Json(name = "per_page") val perPage: Int,
    @Json(name = "total_results") val totalResults: Int,
    @Json(name = "next_page") val nextPage: String?
)
