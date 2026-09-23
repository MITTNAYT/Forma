package com.forma.app.domain.usecase.chronotype

import com.forma.app.core.chronotype.CircadianEnergyEngine
import com.forma.app.domain.model.ChronoAlignmentReport
import com.forma.app.domain.model.ChronoRecommendation
import com.forma.app.domain.model.Chronotype
import com.forma.app.domain.model.EnergyZone
import com.forma.app.domain.model.TodayScheduleItem
import com.forma.app.domain.repository.TimelineRepository
import com.forma.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AlignTimelineWithChronotypeUseCase @Inject constructor(
    private val circadianEnergyEngine: CircadianEnergyEngine,
    private val timelineRepository: TimelineRepository,
    private val preferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): ChronoAlignmentReport {
        val chronotype = preferencesRepository.chronotype.first()
        val dateIso = date.toString()
        val timelineItems = timelineRepository.getTimelineItemsForDate(dateIso).first()
        val (activeZone, currentHourEnergy) = circadianEnergyEngine.getCurrentEnergyState(chronotype)

        val recommendations = mutableListOf<ChronoRecommendation>()
        var totalPoints = 0
        var maxPossiblePoints = 0

        val scheduledItems = timelineItems.filter { it.startTime != null }

        for (item in scheduledItems) {
            val time = try {
                LocalTime.parse(item.startTime, DateTimeFormatter.ofPattern("HH:mm"))
            } catch (_: Exception) {
                null
            } ?: continue

            val hour = time.hour
            val score = circadianEnergyEngine.calculateDailyEnergyCurve(chronotype).getOrNull(hour)?.energyScore ?: 0.5f

            maxPossiblePoints += 100
            totalPoints += (score * 100).toInt()

            // If task is demanding/habit and scheduled during low biological energy (< 0.40)
            if (score < 0.40f) {
                val bestHour = when (chronotype) {
                    Chronotype.LION -> 8
                    Chronotype.BEAR -> 11
                    Chronotype.WOLF -> 18
                    Chronotype.DOLPHIN -> 15
                    Chronotype.BIMODAL_NOCTURNAL -> if (hour < 16) 13 else 23
                }
                val bestHourFormatted = String.format("%02d:00", bestHour)
                recommendations.add(
                    ChronoRecommendation(
                        habitOrTaskId = item.id,
                        title = item.title,
                        currentScheduledTime = item.startTime ?: "",
                        recommendedTime = bestHourFormatted,
                        reason = "Scheduled during a biological dip (${String.format("%.0f", score * 100)}% energy). Moving to $bestHourFormatted aligns with your peak focus window.",
                        boostPercentage = ((0.90f - score) * 100).toInt().coerceAtLeast(15)
                    )
                )
            }
        }

        val alignmentPercentage = if (maxPossiblePoints > 0) {
            (totalPoints * 100 / maxPossiblePoints).coerceIn(10, 100)
        } else {
            85 // Default baseline when starting empty
        }

        return ChronoAlignmentReport(
            chronotype = chronotype,
            alignmentPercentage = alignmentPercentage,
            activeZone = activeZone,
            currentHourEnergy = currentHourEnergy,
            recommendations = recommendations.take(3)
        )
    }
}
