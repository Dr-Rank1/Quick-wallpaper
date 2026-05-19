package com.rank.quickwallpaper.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val thumbUrl: String,
    val regularUrl: String,
    val fullUrl: String,
    val photographerName: String,
    val photographerUsername: String,
    val photographerProfileUrl: String,
    val width: Int,
    val height: Int,
    val color: String?,
    val downloadLocation: String,
    val savedAt: Long = System.currentTimeMillis()
)
