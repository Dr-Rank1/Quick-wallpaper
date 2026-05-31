# Pazia

**Pazia** (Swahili: *curtain / screen*) is a Kotlin Android wallpaper app powered by the Pexels API. Browse millions of curated photos, set them as your wallpaper in one tap, and save your favorites locally.

## Features

- **Editorial feed** — curated photos with infinite scroll (Paging 3)
- **10 categories** — Nature, Architecture, Abstract, Animals, Travel, Minimal, Dark, Cars, Space, Kenya
- **Search** — debounced query search across Pexels' full library
- **Detail screen** — full-screen preview with pinch-to-zoom, photographer attribution
- **Set wallpaper** — Home, Lock, or Both via WallpaperManager
- **Download** — saves to `Pictures/Pazia` via MediaStore (scoped storage compliant)
- **Favorites** — local Room DB, swipe to remove
- **Curated Collections** — browse and view Unsplash collections
- **AdMob** — Native (every 12 items), Interstitial (before download, 4-min cap), App Open (cold start), Rewarded (premium section)
- **Dark mode by default**, Material You dynamic colors on Android 12+
- **Onboarding** — 3-slide skippable intro

## Tech Stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + StateFlow + Repository |
| DI | Hilt |
| Networking | Retrofit 2 + OkHttp + Moshi |
| Images | Coil 2 (250 MB disk cache, 25% mem cache) |
| Paging | Paging 3 |
| Local DB | Room |
| Downloads | Android DownloadManager + BroadcastReceiver |
| Preferences | DataStore |
| Ads | Google AdMob |
| Async | Coroutines + Flow |

## Setup

### 1. Get a Pexels API key

1. Create a free account at [pexels.com](https://www.pexels.com)
2. Go to [www.pexels.com/api](https://www.pexels.com/api/) → **Your API Key**
3. Copy the key (it looks like a long alphanumeric string)

### 2. Add the key to local.properties

```properties
# local.properties  (this file is gitignored — never commit it)
sdk.dir=/path/to/Android/sdk
PEXELS_API_KEY=YOUR_KEY_HERE
```

The key is injected at build time as `BuildConfig.PEXELS_API_KEY` by `app/build.gradle.kts`. It is never hardcoded in source code.

### 3. AdMob setup

1. Log into [AdMob](https://admob.google.com) with publisher ID `ca-app-pub-9418386170210711`
2. Register the app as `com.iankamau.pazia`
3. Create four ad units: **Native Advanced**, **Interstitial**, **App Open**, **Rewarded**
4. Update the release `buildConfigField` entries in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "ADMOB_NATIVE_ID",       "\"ca-app-pub-9418386170210711/YOUR_NATIVE\"")
   buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-9418386170210711/YOUR_INTER\"")
   buildConfigField("String", "ADMOB_APP_OPEN_ID",     "\"ca-app-pub-9418386170210711/YOUR_APPOPEN\"")
   buildConfigField("String", "ADMOB_REWARDED_ID",     "\"ca-app-pub-9418386170210711/YOUR_REWARDED\"")
   ```
5. Replace `manifestPlaceholders["admobAppId"]` in `build.gradle.kts` with your real App ID.

Debug builds use Google's test ad unit IDs automatically.

### 4. Build

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```
com.iankamau.pazia/
├── data/
│   ├── remote/
│   │   ├── PexelsApi.kt            Retrofit interface
│   │   ├── dto/                    PhotoDto, CollectionDto, etc. + Mappers
│   │   └── PhotoPagingSource.kt    Editorial/Search/Category/Collection sources
│   ├── local/
│   │   ├── PaziaDatabase.kt        Room DB
│   │   ├── FavoriteDao.kt
│   │   └── entities/FavoriteEntity.kt
│   └── repository/PhotoRepository.kt
├── domain/model/                   Photo, Photographer, Collection, Category
├── ui/
│   ├── home/                       HomeScreen + ViewModel
│   ├── search/                     SearchScreen + ViewModel (debounced)
│   ├── categories/                 CategoriesScreen + ViewModel
│   ├── detail/                     DetailScreen + ViewModel + WallpaperSheet
│   ├── favorites/                  FavoritesScreen + ViewModel (swipe-to-delete)
│   ├── collections/                CollectionsScreen + CollectionDetailScreen
│   ├── settings/                   SettingsScreen + ViewModel (DataStore)
│   ├── onboarding/                 OnboardingScreen (3 slides, pager)
│   ├── components/                 PhotoGridItem, ShimmerBox, AdSlot, ErrorState
│   ├── theme/                      PaziaTheme, Color, Typography
│   └── NavGraph.kt                 Single-activity Compose Navigation
├── download/
│   ├── WallpaperDownloader.kt      DownloadManager wrapper (enqueue + status query)
│   └── DownloadCompleteReceiver.kt BroadcastReceiver → DownloadEventBus
├── service/
│   └── WallpaperApplier.kt         WallpaperManager wrapper (Home/Lock/Both)
├── ads/                            AppOpenAdManager, InterstitialAdManager, RewardedAdManager
├── di/                             NetworkModule, DatabaseModule, AppModule, WorkerModule
├── MainActivity.kt
└── PaziaApplication.kt             Coil setup, MobileAds init, Hilt WorkManager
```

## Pexels API Compliance

The app follows the Pexels API guidelines:

- Every photo displays `"Photo by [Name] on Pexels"` with a link to the photographer's Pexels profile
- Photos must be attributed — do not imply Pexels or the photographer endorses the app
- The Pexels logo is displayed in the Settings screen attribution row
- Photos are free to use under the [Pexels License](https://www.pexels.com/license/)

## Permissions

| Permission | Purpose |
|------------|---------|
| `INTERNET` | API calls, image loading, AdMob |
| `ACCESS_NETWORK_STATE` | Offline detection |
| `SET_WALLPAPER` | Apply wallpaper (normal permission) |
| `POST_NOTIFICATIONS` | Download progress notification (Android 13+) |
| `WAKE_LOCK` | WorkManager downloads |
| `WRITE_EXTERNAL_STORAGE` | Save photos on Android ≤ 8 only |
| `READ_MEDIA_IMAGES` | Query MediaStore for downloaded images (Android 13+) |

## License

MIT — Ian Gicheha Mbae
