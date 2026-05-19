package com.rank.quickwallpaper.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotoListResponseDto(
    @Json(name = "photos") val photos: List<PhotoDto>,
    @Json(name = "total_results") val totalResults: Int?,
    @Json(name = "page") val page: Int,
    @Json(name = "per_page") val perPage: Int,
    @Json(name = "next_page") val nextPage: String?
)

@JsonClass(generateAdapter = true)
data class CollectionMediaResponseDto(
    @Json(name = "id") val id: String,
    @Json(name = "media") val media: List<PhotoDto>,
    @Json(name = "total_results") val totalResults: Int,
    @Json(name = "next_page") val nextPage: String?
)
