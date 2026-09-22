# Forma ProGuard Rules - Safe Tree Shaking without Obfuscation
-dontobfuscate

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-dontwarn javax.annotation.**

# Keep all Forma App code completely intact
-keep class com.forma.app.** { *; }
-keep interface com.forma.app.** { *; }

# Navigation Compose (prevent route & argument stripping)
-keep class androidx.navigation.** { *; }
-keep interface androidx.navigation.** { *; }

# Compose Runtime, UI & Animations
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }

# Room Database & Entities
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class com.forma.app.data.local.entity.** { *; }
-keep class com.forma.app.domain.model.** { *; }
-dontwarn androidx.room.paging.**

# Kotlin Coroutines & Reflection
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Hilt & Dependency Injection
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends androidx.lifecycle.ViewModel
-keep class com.forma.app.core.di.** { *; }

# WorkManager & Background Sync
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.Worker { *; }

# JSON Serialization
-keep class org.json.** { *; }

