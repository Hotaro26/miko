# Keep Retrofit and Gson classes
-keep class com.miko.reader.model.** { *; }
-keep class com.miko.reader.api.** { *; }

# Prevent obfuscation of Gson SerializedName annotations
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Retrofit rules
-keepattributes RuntimeVisibleAlphaAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Gson rules
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }

# OkHttp rules
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Room related classes
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
