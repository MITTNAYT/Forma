package com.habitflow.app.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Curated Forma Vector Icons for mindful habits, tasks, and daily rituals.
 */
object HabitFlowIcons {
    fun getVector(key: String): ImageVector {
        return when (key.lowercase().trim()) {
            "alarm", "clock", "⏰" -> Icons.Rounded.Alarm
            "timer", "stopwatch", "⏱️" -> Icons.Rounded.Timer
            "schedule", "time" -> Icons.Rounded.Schedule
            "workout", "fitness", "gym", "dumbell" -> Icons.Rounded.FitnessCenter
            "run", "jog", "sprint", "🏃" -> Icons.Rounded.DirectionsRun
            "walk", "step", "stroll", "🚶" -> Icons.Rounded.DirectionsWalk
            "bike", "cycling", "ride", "🚴" -> Icons.Rounded.DirectionsBike
            "meditation", "zen", "mind", "mindfulness", "🧘" -> Icons.Rounded.SelfImprovement
            "spa", "tranquil", "peace", "leaf" -> Icons.Rounded.Spa
            "voice", "mic", "speech", "talk", "podcast", "🎙️" -> Icons.Rounded.Mic
            "code", "dev", "tech", "software", "💻" -> Icons.Rounded.Code
            "work", "briefcase", "career", "office" -> Icons.Rounded.Work
            "sun", "morning", "sunlight", "☀️", "sunrise" -> Icons.Rounded.WbSunny
            "water", "hydration", "drink", "💧" -> Icons.Rounded.WaterDrop
            "book", "reading", "study", "learn", "📖" -> Icons.Rounded.MenuBook
            "school", "academy", "degree" -> Icons.Rounded.School
            "deepwork", "focus", "brain", "think", "intellect" -> Icons.Rounded.Psychology
            "lightbulb", "idea", "creativity", "insight", "💡" -> Icons.Rounded.Lightbulb
            "target", "goal", "aim", "🎯" -> Icons.Rounded.TrackChanges
            "moon", "night", "sleep", "bed", "rest", "🌙" -> Icons.Rounded.Bedtime
            "coffee", "espresso", "cafe", "☕" -> Icons.Rounded.Coffee
            "tea", "cup", "warm" -> Icons.Rounded.LocalCafe
            "food", "meal", "nutrition", "dinner", "lunch" -> Icons.Rounded.Restaurant
            "streak", "fire", "flame", "burn", "🔥" -> Icons.Rounded.LocalFireDepartment
            "sparkles", "ai", "magic", "flow", "✨" -> Icons.Rounded.AutoAwesome
            "inbox", "tray", "archive", "organize" -> Icons.Rounded.Inbox
            "star", "favorite", "priority", "⭐" -> Icons.Rounded.Star
            "heart", "love", "health", "care", "❤️" -> Icons.Rounded.Favorite
            "pin", "note", "reminder", "📌" -> Icons.Rounded.PushPin
            "event", "calendar", "date", "plan" -> Icons.Rounded.Event
            "edit", "journal", "write", "draft" -> Icons.Rounded.Edit
            "brush", "art", "design", "sketch" -> Icons.Rounded.Brush
            "palette", "color", "theme" -> Icons.Rounded.Palette
            "music", "melody", "audio", "song", "🎵" -> Icons.Rounded.MusicNote
            "home", "house", "sanctuary" -> Icons.Rounded.Home
            "flag", "milestone" -> Icons.Rounded.Flag
            "bookmark", "save" -> Icons.Rounded.Bookmark
            else -> Icons.Rounded.CheckCircle
        }
    }

    /**
     * Curated list of all 36+ categorized icons with friendly labels
     */
    val allIcons = listOf(
        Triple("zen", "Meditation", "Mindfulness"),
        Triple("spa", "Tranquil", "Mindfulness"),
        Triple("sun", "Morning Sunlight", "Mindfulness"),
        Triple("moon", "Night Rest", "Mindfulness"),
        Triple("water", "Hydration", "Mindfulness"),
        Triple("heart", "Wellness", "Mindfulness"),

        Triple("deepwork", "Deep Focus", "Productivity"),
        Triple("code", "Code / Dev", "Productivity"),
        Triple("target", "Goal Focus", "Productivity"),
        Triple("work", "Work Session", "Productivity"),
        Triple("edit", "Journal / Writing", "Productivity"),
        Triple("lightbulb", "Idea / Creation", "Productivity"),
        Triple("pin", "Quick Intention", "Productivity"),
        Triple("event", "Event / Plan", "Productivity"),
        Triple("flag", "Milestone", "Productivity"),
        Triple("sparkles", "AI Synthesis", "Productivity"),

        Triple("workout", "Strength Fitness", "Active"),
        Triple("run", "Running / Cardio", "Active"),
        Triple("walk", "Mindful Walk", "Active"),
        Triple("bike", "Cycling", "Active"),
        Triple("fire", "High Intensity", "Active"),

        Triple("book", "Reading Book", "Learning"),
        Triple("school", "Study / Course", "Learning"),
        Triple("timer", "Pomodoro Time", "Learning"),
        Triple("alarm", "Scheduled Alarm", "Learning"),

        Triple("coffee", "Coffee Break", "Lifestyle"),
        Triple("tea", "Tea Ritual", "Lifestyle"),
        Triple("food", "Mindful Meal", "Lifestyle"),
        Triple("home", "Sanctuary / Home", "Lifestyle"),
        Triple("music", "Music / Listening", "Lifestyle"),
        Triple("brush", "Art & Design", "Lifestyle"),
        Triple("star", "Special Ritual", "Lifestyle")
    )
}

typealias FormaIcons = HabitFlowIcons

@Composable
fun FormaIcon(
    iconKey: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Icon(
        imageVector = HabitFlowIcons.getVector(iconKey),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun HabitFlowIcon(
    iconKey: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    FormaIcon(
        iconKey = iconKey,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
