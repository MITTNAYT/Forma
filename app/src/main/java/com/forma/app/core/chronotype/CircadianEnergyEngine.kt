package com.forma.app.core.chronotype

import com.forma.app.domain.model.Chronotype
import com.forma.app.domain.model.EnergyZone
import com.forma.app.domain.model.HourlyEnergyPoint
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/**
 * Circadian Energy Wave Engine.
 * Models biological cognitive alertness and energy curves over 24 hours.
 */
@Singleton
class CircadianEnergyEngine @Inject constructor() {

    /**
     * Computes the 24-hour continuous energy curve (0.0 to 1.0) for a given chronotype.
     */
    fun calculateDailyEnergyCurve(chronotype: Chronotype): List<HourlyEnergyPoint> {
        return (0..23).map { hour ->
            val score = computeEnergyScore(hour, chronotype)
            val zone = determineZone(hour, score, chronotype)
            HourlyEnergyPoint(
                hour = hour,
                energyScore = score,
                zone = zone
            )
        }
    }

    /**
     * Determines current active energy zone and score based on current time.
     */
    fun getCurrentEnergyState(chronotype: Chronotype, time: LocalTime = LocalTime.now()): Pair<EnergyZone, Float> {
        val hour = time.hour
        val score = computeEnergyScore(hour, chronotype)
        val zone = determineZone(hour, score, chronotype)
        return Pair(zone, score)
    }

    private fun computeEnergyScore(hour: Int, chronotype: Chronotype): Float {
        // Base Gaussian biological wave modeling
        val score = when (chronotype) {
            Chronotype.LION -> {
                // Peak around 7-8 AM, secondary peak at 11 AM, steep decline after 6 PM
                val morningPeak = gaussian(hour.toDouble(), peakHour = 7.5, width = 3.0) * 0.95
                val midDayPeak = gaussian(hour.toDouble(), peakHour = 12.0, width = 2.5) * 0.70
                val baseline = if (hour in 5..20) 0.30 else 0.10
                max(baseline, max(morningPeak, midDayPeak))
            }
            Chronotype.BEAR -> {
                // Solar peak around 11 AM - 1 PM, afternoon dip at 3 PM, gentle evening decline
                val primaryPeak = gaussian(hour.toDouble(), peakHour = 11.5, width = 3.5) * 0.92
                val secondaryPeak = gaussian(hour.toDouble(), peakHour = 16.5, width = 2.0) * 0.65
                val baseline = if (hour in 7..22) 0.32 else 0.08
                max(baseline, max(primaryPeak, secondaryPeak))
            }
            Chronotype.WOLF -> {
                // Slow morning rise, primary peak at 18:00 (6 PM) - 21:00 (9 PM)
                val morningSlow = gaussian(hour.toDouble(), peakHour = 13.0, width = 3.0) * 0.60
                val eveningSurge = gaussian(hour.toDouble(), peakHour = 19.0, width = 3.2) * 0.96
                val baseline = if (hour in 9..24 || hour in 0..1) 0.35 else 0.12
                max(baseline, max(morningSlow, eveningSurge))
            }
            Chronotype.DOLPHIN -> {
                // Spurt peaks around 10:30 AM and 15:30 (3:30 PM)
                val morningBurst = gaussian(hour.toDouble(), peakHour = 10.5, width = 2.0) * 0.85
                val afternoonBurst = gaussian(hour.toDouble(), peakHour = 15.5, width = 2.2) * 0.88
                val baseline = if (hour in 8..22) 0.38 else 0.15
                max(baseline, max(morningBurst, afternoonBurst))
            }
        }
        return min(1.0, max(0.05, score)).toFloat()
    }

    private fun determineZone(hour: Int, score: Float, chronotype: Chronotype): EnergyZone {
        return when {
            score >= 0.75f -> EnergyZone.PEAK_FOCUS
            score in 0.50f..0.74f -> EnergyZone.CREATIVE_FLOW
            hour >= chronotype.windDownHour || hour < 5 -> EnergyZone.WIND_DOWN
            else -> EnergyZone.RECHARGE_REST
        }
    }

    private fun gaussian(x: Double, peakHour: Double, width: Double): Double {
        val diff = x - peakHour
        return exp(-(diff * diff) / (2 * width * width))
    }
}
