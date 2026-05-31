package com.rank.quickwallpaper.data.remote.dto

import com.rank.quickwallpaper.domain.model.Collection
import com.rank.quickwallpaper.domain.model.Photo
import com.rank.quickwallpaper.domain.model.Photographer

fun PhotoDto.toDomain(): Photo = Photo(
    id = id.toString(),
    width = width,
    height = height,
    description = alt,
    likes = 0,
    downloads = 0,
    thumbUrl = src.medium,
    smallUrl = src.large,
    regularUrl = src.large2x,
    fullUrl = src.original,
    rawUrl = src.original,
        photographer = Photographer(
            id = photographerId.toString(),
            username = if ("@" in photographerUrl)
            photographerUrl.substringAfterLast("@").trimEnd('/')
        else
            photographerUrl.trimEnd('/').substringAfterLast('/'),
        name = photographer,
        profileUrl = photographerUrl,
        avatarUrl = null
    ),
    color = avgColor,
    downloadLocation = "",
    htmlLink = url
)

fun CollectionDto.toDomain(): Collection = Collection(
    id = id,
    title = title,
    description = description,
    totalPhotos = photosCount,
    coverPhotoUrl = null,
    coverColor = null,
    curatorName = "",
    curatorUsername = ""
)
