package com.forma.app.core.util

object Constants {
    const val DATABASE_NAME = "habitflow_db"
    const val PREFERENCES_NAME = "habitflow_preferences"

    const val NOTIFICATION_CHANNEL_HABITS_ID = "forma_habits_channel"
    const val NOTIFICATION_CHANNEL_HABITS_NAME = "Forma Ritual Reminders"
    const val NOTIFICATION_CHANNEL_TASKS_ID = "forma_tasks_channel"
    const val NOTIFICATION_CHANNEL_TASKS_NAME = "Forma Task Reminders"

    // Signature Behance natural palette tags
    val COLOR_TAGS = listOf(
        "#485938", // Forest Sage Green
        "#EAA036", // Warm Amber Gold
        "#E0E9DB", // Soft Sage Tint
        "#FDEEDB", // Soft Peach Tint
        "#6B8258", // Olive Green
        "#1E211E"  // Deep Espresso
    )

    // Vector icon keys for Habits & Tasks
    val DEFAULT_HABIT_ICONS = listOf(
        "sun", "water", "meditation", "workout", "code", "book", "voice", "target", "coffee", "moon", "flame", "star"
    )

    val DEFAULT_TASK_ICONS = listOf(
        "pin", "code", "calendar", "workout", "voice", "meditation", "target", "coffee", "book", "star"
    )
}
