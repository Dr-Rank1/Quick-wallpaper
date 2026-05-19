package com.rank.quickwallpaper.domain.model

data class Collection(
    val id: String,
    val title: String,
    val description: String?,
    val totalPhotos: Int,
    val coverPhotoUrl: String?,
    val coverColor: String?,
    val curatorName: String,
    val curatorUsername: String
)
