# Keep line numbers for readable crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions

# Retrofit 2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# TMDB API response models and app models (Gson deserialization targets)
-keep class emplay.entertainment.emplay.api.** { *; }
-keep class emplay.entertainment.emplay.models.** { *; }

# Firebase Auth
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google Sign-In / Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.**

# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**
