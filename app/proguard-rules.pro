# NUCLEAR PROGUARD STRATEGY - Keep everything in the app
-keep class com.miko.reader.** { *; }
-keep interface com.miko.reader.** { *; }

# Keep all Room generated code and implementation
-keep class androidx.room.** { *; }
-keep class com.miko.reader.model.**_Impl { *; }
-keep @androidx.room.Database class *
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep Retrofit, OkHttp, and Gson entirely
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep @retrofit2.http.* interface * { *; }

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken
-keep @com.google.gson.annotations.SerializedName class * { *; }

# Keep Kotlin components and Metadata
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.serialization.** { *; }

# Keep Android/Google components that might be stripped
-keep class com.google.android.gms.** { *; }
-keep class androidx.** { *; }

# Preserve all attributes
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses, SourceFile, LineNumberTable

# Support default parameters in Kotlin
-keepclassmembers class * {
    *** *($$default);
}

# Don't warn about anything
-dontwarn **
