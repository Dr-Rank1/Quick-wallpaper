import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.rank.quickwallpaper"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.rank.quickwallpaper"
        minSdk = 24
        targetSdk = 37
        versionCode = 15
        versionName = "1.0.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        buildConfigField(
            "String",
            "PEXELS_API_KEY",
            "\"${localProps.getProperty("PEXELS_API_KEY", "YOUR_KEY_HERE")}\""
        )

        buildConfigField("String", "UNITY_GAME_ID", "\"6211137\"")

    }

    signingConfigs {
        val ksFile = rootProject.file("keystore.properties")
        val ksProps = Properties().apply {
            if (ksFile.exists()) load(ksFile.inputStream())
        }
        val keystorePath = ksProps.getProperty("storeFile")
            ?: localProps.getProperty("KEYSTORE_PATH")
            ?: "upload-keystore.jks"
        val keystoreFile = rootProject.file(keystorePath)
        if (keystoreFile.exists()) {
            create("release") {
                storeFile = keystoreFile
                storePassword = ksProps.getProperty("storePassword")
                    ?: localProps.getProperty("KEYSTORE_PASSWORD")
                    ?: "QuickWallpaper2026!"
                keyAlias = ksProps.getProperty("keyAlias")
                    ?: localProps.getProperty("KEY_ALIAS")
                    ?: "upload"
                keyPassword = ksProps.getProperty("keyPassword")
                    ?: localProps.getProperty("KEY_PASSWORD")
                    ?: "QuickWallpaper2026!"
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint { baseline = file("lint-baseline.xml") }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.room.paging)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Glance widgets
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // Retrofit + Moshi
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.core)
    ksp(libs.moshi.codegen)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Paging 3
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Unity Ads
    implementation(libs.unity.ads)

    // DataStore
    implementation(libs.datastore.preferences)

    // Accompanist
    implementation(libs.accompanist.permissions)

    // Testing
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation("androidx.room:room-testing:${libs.versions.room.get()}")
}
