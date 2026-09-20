# ProGuard Rules for Release AAB & APK Builds

# Keep Kotlin metadata and annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Firebase and Firestore Models
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** { *; }

-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }

-keep class com.example.util.** { *; }
-keepclassmembers class com.example.util.** { *; }

# Firestore annotations & serialization
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.IgnoreExtraProperties <fields>;
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}

-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Google Mobile Ads (AdMob)
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Room Database
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
