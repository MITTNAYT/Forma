# HabitFlow ProGuard Rules
-keepattributes *Annotation*
-dontwarn javax.annotation.**

# Room Database & Entities
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class com.habitflow.app.data.local.entity.** { *; }
-keep class com.habitflow.app.domain.model.** { *; }
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Hilt & Dependency Injection
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends androidx.lifecycle.ViewModel
