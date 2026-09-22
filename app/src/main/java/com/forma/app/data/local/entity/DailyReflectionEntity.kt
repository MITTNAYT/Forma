package com.forma.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.forma.app.domain.model.DailyReflection
import com.forma.app.domain.model.EnergyLevel

@Entity(tableName = "daily_reflections")
data class DailyReflectionEntity(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val keystoneIntentions: String, // Comma or newline separated
    val gratitudeNote: String,
    val mindfulnessScore: Int,
    val energyLevel: String,
    val isMorningCompleted: Boolean,
    val isEveningCompleted: Boolean,
    val updatedAt: Long
) {
    fun toDomain(): DailyReflection {
        val intentions = if (keystoneIntentions.isBlank()) {
            emptyList()
        } else {
            keystoneIntentions.split("\n").filter { it.isNotBlank() }
        }
        return DailyReflection(
            date = date,
            keystoneIntentions = intentions,
            gratitudeNote = gratitudeNote,
            mindfulnessScore = mindfulnessScore,
            energyLevel = try { EnergyLevel.valueOf(energyLevel) } catch (_: Exception) { EnergyLevel.MEDIUM },
            isMorningCompleted = isMorningCompleted,
            isEveningCompleted = isEveningCompleted,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(reflection: DailyReflection): DailyReflectionEntity {
            return DailyReflectionEntity(
                date = reflection.date,
                keystoneIntentions = reflection.keystoneIntentions.joinToString("\n"),
                gratitudeNote = reflection.gratitudeNote,
                mindfulnessScore = reflection.mindfulnessScore,
                energyLevel = reflection.energyLevel.name,
                isMorningCompleted = reflection.isMorningCompleted,
                isEveningCompleted = reflection.isEveningCompleted,
                updatedAt = reflection.updatedAt
            )
        }
    }
}
