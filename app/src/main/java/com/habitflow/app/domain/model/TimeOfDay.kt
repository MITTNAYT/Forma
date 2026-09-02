package com.habitflow.app.domain.model

enum class TimeOfDay(val displayName: String, val sortOrder: Int) {
    MORNING("Morning", 1),
    AFTERNOON("Afternoon", 2),
    EVENING("Evening", 3),
    ANYTIME("Anytime", 4)
}
