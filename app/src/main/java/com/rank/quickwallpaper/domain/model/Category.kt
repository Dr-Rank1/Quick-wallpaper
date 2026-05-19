package com.rank.quickwallpaper.domain.model

data class Category(
    val id: String,
    val displayName: String,
    val query: String,
    val emoji: String
)

val ALL_CATEGORIES = listOf(
    Category("nature",       "Nature",        "nature",        "🌿"),
    Category("architecture", "Architecture",  "architecture",  "🏛️"),
    Category("abstract",     "Abstract",      "abstract",      "🎨"),
    Category("animals",      "Animals",       "animals",       "🦁"),
    Category("travel",       "Travel",        "travel",        "✈️"),
    Category("minimal",      "Minimal",       "minimal",       "⬜"),
    Category("dark",         "Dark",          "dark aesthetic","🖤"),
    Category("cars",         "Cars",          "cars",          "🚗"),
    Category("space",        "Space",         "space cosmos",  "🚀"),
    Category("kenya",        "Kenya",         "Kenya",         "🇰🇪")
)
