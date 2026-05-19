package com.rank.quickwallpaper.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotoDto(
    @Json(name = "id") val id: Long,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int,
    @Json(name = "url") val url: String,
    @Json(name = "photographer") val photographer: String,
    @Json(name = "photographer_url") val photographerUrl: String,
    @Json(name = "photographer_id") val photographerId: Long,
    @Json(name = "avg_color") val avgColor: String?,
    @Json(name = "src") val src: SrcDto,
    @Json(name = "alt") val alt: String?,
    @Json(name = "liked") val liked: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SrcDto(
    @Json(name = "original") val original: String,
    @Json(name = "large2x") val large2x: String,
    @Json(name = "large") val large: String,
    @Json(name = "medium") val medium: String,
    @Json(name = "small") val small: String,
    @Json(name = "portrait") val portrait: String,
    @Json(name = "landscape") val landscape: String,
    @Json(name = "tiny") val tiny: String
)
