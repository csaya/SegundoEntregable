# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============ ROOM Database ============
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============ GSON ============
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep data models for GSON
-keep class com.example.segundoentregable.data.model.** { *; }
-keep class com.example.segundoentregable.data.local.entity.** { *; }

# ============ Coil ============
-dontwarn coil.**
-keep class coil.** { *; }

# ============ Google Maps ============
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.maps.android.** { *; }
-dontwarn com.google.android.gms.**

# ============ Kotlin Coroutines ============
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============ Compose ============
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ============ Navigation ============
-keep class * extends androidx.navigation.Navigator

# ============ Firebase ============
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-keepattributes *Annotation*

# ============ WorkManager ============
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker