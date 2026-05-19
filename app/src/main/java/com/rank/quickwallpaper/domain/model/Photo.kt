package com.rank.quickwallpaper.domain.model

data class Photo(
    val id: String,
    val width: Int,
    val height: Int,
    val description: String?,
    val likes: Int,
    val downloads: Int,
    val thumbUrl: String,
    val smallUrl: String,
    val regularUrl: String,
    val fullUrl: String,
    val rawUrl: String,
    val photographer: Photographer,
    val color: String?,
    val downloadLocation: String,
    val htmlLink: String
) {
    val aspectRatio: Float get() = width.toFloat() / height.toFloat()
}

data class Photographer(
    val id: String,
    val username: String,
    val name: String,
    val profileUrl: String,
    val avatarUrl: String?
)
