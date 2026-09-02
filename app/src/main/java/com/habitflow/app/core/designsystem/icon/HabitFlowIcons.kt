package com.habitflow.app.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object HabitFlowIcons {
    fun getVector(key: String): ImageVector {
        return when (key.lowercase().trim()) {
            "alarm", "clock", "⏰", "time" -> Icons.Rounded.Alarm
            "workout", "fitness", "exercise", "run", "🏃", "gym" -> Icons.Rounded.FitnessCenter
            "walk", "step", "🚶" -> Icons.Rounded.DirectionsWalk
            "meditation", "zen", "mind", "mindfulness", "☯️", "🧘" -> Icons.Rounded.SelfImprovement
            "voice", "mic", "speech", "talk", "podcast", "🎙️", "🎤" -> Icons.Rounded.Mic
            "code", "dev", "fullstack", "programming", "chip", "💻", "🧠" -> Icons.Rounded.Code
            "sun", "morning", "sunlight", "☀️", "sunrise" -> Icons.Rounded.WbSunny
            "water", "hydration", "drink", "💧" -> Icons.Rounded.WaterDrop
            "book", "reading", "study", "learn", "journal", "📖" -> Icons.Rounded.MenuBook
            "deepwork", "focus", "brain", "think" -> Icons.Rounded.Psychology
            "target", "goal", "aim", "🎯" -> Icons.Rounded.TrackChanges
            "moon", "night", "sleep", "bed", "rest", "🌙" -> Icons.Rounded.Bedtime
            "coffee", "tea", "break", "cafe", "☕" -> Icons.Rounded.Coffee
            "streak", "fire", "flame", "🔥" -> Icons.Rounded.LocalFireDepartment
            "sparkles", "ai", "magic", "✨" -> Icons.Rounded.AutoAwesome
            "inbox", "tray", "archive", "📥" -> Icons.Rounded.Inbox
            "star", "favorite", "pro", "⭐" -> Icons.Rounded.Star
            "pin", "note", "📌" -> Icons.Rounded.PushPin
            else -> Icons.Rounded.CheckCircle
        }
    }

    val availableHabitIcons = listOf(
        "sun" to "Morning Sunlight",
        "water" to "Hydration",
        "meditation" to "Meditation / Zen",
        "workout" to "Fitness / Exercise",
        "code" to "Deep Work / Code",
        "book" to "Reading / Learning",
        "voice" to "Articulation / Voice",
        "target" to "Goal Focus",
        "coffee" to "Mindful Break",
        "moon" to "Evening Wind Down"
    )
}

@Composable
fun HabitFlowIcon(
    iconKey: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Icon(
        imageVector = HabitFlowIcons.getVector(iconKey),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}
