package com.rank.quickwallpaper.domain.model

data class Category(
    val id: String,
    val displayName: String,
    val query: String,
    val emoji: String,
    val tagline: String = "",
    val gradientStart: Long = 0xFF1E3A5FL,
    val gradientEnd: Long = 0xFF0D1B2AL,
    val highlightBadge: String = "4K"
)

val ALL_CATEGORIES = listOf(
    Category(
        id = "amoled",
        displayName = "AMOLED",
        query = "amoled pure black wallpaper",
        emoji = "🌑",
        tagline = "True obsidian blacks & battery-saving aesthetics",
        gradientStart = 0xFF0A0A0AL,
        gradientEnd = 0xFF1A1A24L,
        highlightBadge = "OLED"
    ),
    Category(
        id = "nature",
        displayName = "Nature",
        query = "nature landscape scenery",
        emoji = "🌿",
        tagline = "Lush wilderness, misty peaks & serene waters",
        gradientStart = 0xFF064E3BL,
        gradientEnd = 0xFF047857L,
        highlightBadge = "Popular"
    ),
    Category(
        id = "architecture",
        displayName = "Architecture",
        query = "architecture city building",
        emoji = "🏛️",
        tagline = "Modern geometric lines & timeless cityscapes",
        gradientStart = 0xFF4C1D95L,
        gradientEnd = 0xFF6D28D9L,
        highlightBadge = "Design"
    ),
    Category(
        id = "abstract",
        displayName = "Abstract",
        query = "abstract 3d fluid wallpaper",
        emoji = "🎨",
        tagline = "Vibrant fluid colorways & mesmerizing shapes",
        gradientStart = 0xFF831843L,
        gradientEnd = 0xFFBE185DL,
        highlightBadge = "Art"
    ),
    Category(
        id = "animals",
        displayName = "Animals",
        query = "wildlife animals close up portrait",
        emoji = "🦁",
        tagline = "Majestic creatures captured in their raw habitat",
        gradientStart = 0xFF78350FL,
        gradientEnd = 0xFFB45309L,
        highlightBadge = "Wild"
    ),
    Category(
        id = "travel",
        displayName = "Travel",
        query = "travel scenic wanderlust destination",
        emoji = "✈️",
        tagline = "Iconic landmarks & distant dreamy destinations",
        gradientStart = 0xFF0C4A6EL,
        gradientEnd = 0xFF0369A1L,
        highlightBadge = "Scenic"
    ),
    Category(
        id = "minimal",
        displayName = "Minimal",
        query = "minimal clean modern wallpaper",
        emoji = "⬜",
        tagline = "Less is more: clean compositions & calming space",
        gradientStart = 0xFF1F2937L,
        gradientEnd = 0xFF374151L,
        highlightBadge = "Clean"
    ),
    Category(
        id = "dark",
        displayName = "Dark",
        query = "dark aesthetic night moody wallpaper",
        emoji = "🖤",
        tagline = "Moody, cinematic silhouettes & nighttime shadows",
        gradientStart = 0xFF111827L,
        gradientEnd = 0xFF1F2937L,
        highlightBadge = "Moody"
    ),
    Category(
        id = "cars",
        displayName = "Cars",
        query = "supercars automotive aesthetic",
        emoji = "🚗",
        tagline = "Precision engineering, speed & automotive design",
        gradientStart = 0xFF881337L,
        gradientEnd = 0xFFE11D48L,
        highlightBadge = "Speed"
    ),
    Category(
        id = "space",
        displayName = "Space",
        query = "space cosmos galaxy astronomy",
        emoji = "🚀",
        tagline = "Deep universe, radiant nebulae & infinite stars",
        gradientStart = 0xFF172554L,
        gradientEnd = 0xFF1E3A8AL,
        highlightBadge = "Cosmic"
    ),
    Category(
        id = "kenya",
        displayName = "Kenya",
        query = "Kenya wildlife landscape Nairobi",
        emoji = "🇰🇪",
        tagline = "Golden savannahs, rich heritage & African majesty",
        gradientStart = 0xFF14532DL,
        gradientEnd = 0xFF15803DL,
        highlightBadge = "Special"
    )
)
