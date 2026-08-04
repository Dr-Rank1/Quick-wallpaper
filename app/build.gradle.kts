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
        targetSdk = 36
        versionCode = 13
        versionName = "1.0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        buildConfigField(
            "String",
            "PEXELS_API_KEY",
            "\"${localProps.getProperty("PEXELS_API_KEY", "YOUR_KEY_HERE")}\""
        )

    }

    signingConfigs {
        val keystorePath = localProps.getProperty("KEYSTORE_PATH")
        if (!keystorePath.isNullOrBlank()) {
            create("release") {
                storeFile = rootProject.file(keystorePath)
                storePassword = localProps.getProperty("KEYSTORE_PASSWORD")
                keyAlias = localProps.getProperty("KEY_ALIAS")
                keyPassword = localProps.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_BANNER_ID",       "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ADMOB_NATIVE_ID",       "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADMOB_APP_OPEN_ID",     "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "ADMOB_REWARDED_ID",     "\"ca-app-pub-3940256099942544/5224354917\"")
        }
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_BANNER_ID",       "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ADMOB_NATIVE_ID",       "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "ADMOB_APP_OPEN_ID",     "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "ADMOB_REWARDED_ID",     "\"ca-app-pub-3940256099942544/5224354917\"")
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

    // AdMob
    implementation(libs.admob)
    implementation(libs.constraintlayout)

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
