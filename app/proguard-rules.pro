# Keep app models annotated with @JsonClass (Moshi)
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

# Retrofit
-keepattributes Signature, Exceptions

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Moshi
-dontwarn com.squareup.moshi.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.**

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**

# Coil
-dontwarn coil.**

# Start.io
-keep class com.startapp.** { *; }
-keep class com.truenet.** { *; }
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile,
LineNumberTable, *Annotation*, EnclosingMethod
-dontwarn android.webkit.JavascriptInterface
-dontwarn com.startapp.**
-dontwarn org.jetbrains.annotations.**

# Enum classes (used in when expressions)
-keepclassmembers enum * { *; }

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
