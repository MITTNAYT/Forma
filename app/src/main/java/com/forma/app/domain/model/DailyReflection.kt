package com.forma.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DailyReflection(
    val date: String, // YYYY-MM-DD
    val keystoneIntentions: List<String> = emptyList(),
    val gratitudeNote: String = "",
    val mindfulnessScore: Int = 5, // 1 - 5
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val isMorningCompleted: Boolean = false,
    val isEveningCompleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
