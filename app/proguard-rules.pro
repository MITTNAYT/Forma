# HabitFlow ProGuard Rules
-keepattributes *Annotation*
-dontwarn javax.annotation.**
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
