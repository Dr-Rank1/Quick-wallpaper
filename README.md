# Pazia Wallpaper App

Pazia (Swahili for curtain or screen) is a native Android wallpaper application built with Kotlin and Jetpack Compose. It leverages the Pexels API to provide users with a massive library of high-quality, curated photographs that can be easily set as home or lock screen wallpapers.

## Key Features

- **Infinite Scrolling Editorial Feed:** Browse curated, high-resolution photos using Paging 3.
- **Categorized Collections:** Explore diverse categories such as Nature, Architecture, Abstract, Animals, Travel, Minimal, Dark, Cars, Space, and Kenya.
- **Advanced Search:** Utilize debounced query searching across the entire Pexels library.
- **Immersive Detail View:** Enjoy full-screen previews with pinch-to-zoom capabilities and proper photographer attribution.
- **Seamless Wallpaper Application:** Set images as your Home screen, Lock screen, or both directly from the app via the Android WallpaperManager.
- **Local Downloads:** Save wallpapers locally to your device via MediaStore in a scoped storage compliant manner.
- **Favorites System:** Save and manage your favorite wallpapers locally using Room Database, complete with swipe-to-delete functionality.
- **Monetization Integration:** Integrated with Start.io for Native, Interstitial, Splash, and Rewarded ads.
- **Modern UI/UX:** Built with a dark mode default and Material You dynamic color support for Android 12 and above.
- **Onboarding Experience:** Includes a clean, skippable three-slide introduction for new users.

## Technology Stack

- **UI Framework:** Jetpack Compose and Material 3
- **Architecture:** MVVM (Model-View-ViewModel), StateFlow, and Repository Pattern
- **Dependency Injection:** Hilt
- **Networking:** Retrofit 2, OkHttp, and Moshi
- **Image Loading:** Coil 2 (with extensive disk and memory caching)
- **Pagination:** Paging 3
- **Local Storage:** Room Database
- **Downloads:** Android DownloadManager and BroadcastReceiver
- **Preferences:** Jetpack DataStore
- **Asynchronous Programming:** Kotlin Coroutines and Flow

## Getting Started

### 1. Pexels API Configuration

To build the project, you must provide your own Pexels API key.
1. Create a free account on the Pexels website.
2. Navigate to the API section to generate your API key.
3. Create a file named `local.properties` in the root of the project directory if it does not exist.
4. Add your API key to the file:
```properties
PEXELS_API_KEY=YOUR_API_KEY_HERE
```
The build system will automatically inject this key into the app at compile time.

### 2. Start.io Ad Network Setup

1. Register your application on the Start.io portal with the package name `com.rank.quickwallpaper`.
2. Retrieve your Start.io App ID.
3. Add the App ID to your `app/build.gradle.kts` configuration.
4. For production, ensure the supplier list from `app-ads.txt` is hosted on your domain.

### 3. Building and Running

Compile and run the application using Gradle:
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Project Architecture

The application is structured into clearly defined modules and packages:
- **data:** Remote API interfaces, Data Transfer Objects (DTOs), local Room database entities, and repository implementations.
- **domain:** Core business models including Photo, Photographer, and Category.
- **ui:** Jetpack Compose screens organized by feature (home, search, categories, detail, favorites, settings) along with custom components, theming, and navigation graphs.
- **download:** DownloadManager wrappers and event receivers.
- **service:** Wrappers for applying wallpapers to the system.
- **ads:** Ad management and initialization logic for Start.io.
- **di:** Hilt modules providing dependencies for network, database, app context, and background workers.

## Pexels API Compliance

Pazia adheres strictly to Pexels API guidelines:
- Clear attribution is provided for every photograph, linking back to the photographer's Pexels profile.
- There is no implied endorsement by Pexels or the photographers.
- The Pexels logo is prominently displayed in the application settings.
- All photos are utilized under the standard Pexels License.

## License

This project is licensed under the MIT License - Ian Gicheha Mbae.
