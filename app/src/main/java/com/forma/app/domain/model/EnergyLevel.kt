package com.forma.app.domain.model

enum class EnergyLevel(val displayName: String, val dotCount: Int) {
    LOW("Low Energy", 1),
    MEDIUM("Medium Energy", 2),
    HIGH("High Energy", 3)
}
