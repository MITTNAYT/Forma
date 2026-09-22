package com.forma.app.domain.model

/**
 * Diurnal Circadian Chronotypes based on Michael Breus's biological sleep/energy research.
 */
enum class Chronotype(
    val displayName: String,
    val animalSymbol: String,
    val description: String,
    val peakFocusWindow: String,
    val creativeWindow: String,
    val rechargeWindow: String,
    val windDownHour: Int
) {
    LION(
        displayName = "Lion (Early Morning)",
        animalSymbol = "🦁",
        description = "Early riser with immense morning energy and structured focus before noon.",
        peakFocusWindow = "06:00 – 10:00",
        creativeWindow = "10:00 – 14:00",
        rechargeWindow = "14:00 – 17:00",
        windDownHour = 21
    ),
    BEAR(
        displayName = "Bear (Solar Rhythm)",
        animalSymbol = "🐻",
        description = "Follows the sun cycle. Peak productivity mid-morning and steady afternoon momentum.",
        peakFocusWindow = "10:00 – 14:00",
        creativeWindow = "14:00 – 17:00",
        rechargeWindow = "17:00 – 19:00",
        windDownHour = 22
    ),
    WOLF(
        displayName = "Wolf (Evening & Night)",
        animalSymbol = "🐺",
        description = "Slow morning ramp with powerful deep work surges in late afternoon and evening.",
        peakFocusWindow = "16:00 – 21:00",
        creativeWindow = "12:00 – 16:00",
        rechargeWindow = "21:00 – 23:00",
        windDownHour = 24
    ),
    DOLPHIN(
        displayName = "Dolphin (Variable Rhythm)",
        animalSymbol = "🐬",
        description = "High intelligence, light sleeper. Thrives on mid-afternoon bursts and flexible sprints.",
        peakFocusWindow = "14:00 – 18:00",
        creativeWindow = "10:00 – 13:00",
        rechargeWindow = "18:00 – 20:00",
        windDownHour = 23
    )
}

/**
 * Biological Energy Zones mapped throughout the day.
 */
enum class EnergyZone(
    val displayName: String,
    val iconKey: String,
    val subtitle: String
) {
    PEAK_FOCUS(
        displayName = "Peak Focus",
        iconKey = "flame",
        subtitle = "Optimal window for deep, high-cognition rituals & complex tasks"
    ),
    CREATIVE_FLOW(
        displayName = "Creative Flow",
        iconKey = "brush",
        subtitle = "Ideal for design, brainstorming, writing & generative habits"
    ),
    RECHARGE_REST(
        displayName = "Recharge & Rest",
        iconKey = "spa",
        subtitle = "Best for light walks, hydration, breathwork & tea rituals"
    ),
    WIND_DOWN(
        displayName = "Wind Down",
        iconKey = "bedtime",
        subtitle = "Evening sanctuary, journaling, reflection & screen detox"
    )
}

data class HourlyEnergyPoint(
    val hour: Int,
    val energyScore: Float, // 0.0 to 1.0
    val zone: EnergyZone
)

data class ChronoAlignmentReport(
    val chronotype: Chronotype,
    val alignmentPercentage: Int,
    val activeZone: EnergyZone,
    val currentHourEnergy: Float,
    val recommendations: List<ChronoRecommendation>
)

data class ChronoRecommendation(
    val habitOrTaskId: String,
    val title: String,
    val currentScheduledTime: String,
    val recommendedTime: String,
    val reason: String,
    val boostPercentage: Int
)
