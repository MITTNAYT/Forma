package com.forma.app.core.chronotype

import com.forma.app.domain.model.Chronotype
import com.forma.app.domain.model.EnergyZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class CircadianEnergyEngineTest {

    private val engine = CircadianEnergyEngine()

    @Test
    fun bimodalNocturnal_hasMiddayPeak() {
        // At 13:00 (1:00 PM), midday peak should have high energy
        val (zone, score) = engine.getCurrentEnergyState(Chronotype.BIMODAL_NOCTURNAL, LocalTime.of(13, 0))
        assertTrue("Midday score at 13:00 should be >= 0.80, was $score", score >= 0.80f)
        assertEquals(EnergyZone.PEAK_FOCUS, zone)
    }

    @Test
    fun bimodalNocturnal_hasLateNightPeak() {
        // At 00:30 (12:30 AM), nocturnal peak should have high energy
        val (zone, score) = engine.getCurrentEnergyState(Chronotype.BIMODAL_NOCTURNAL, LocalTime.of(0, 30))
        assertTrue("Night score at 00:30 should be >= 0.85, was $score", score >= 0.85f)
        assertEquals(EnergyZone.PEAK_FOCUS, zone)

        // At 23:00 (11:00 PM), nocturnal peak should be in Peak Focus or Creative Flow
        val (zone23, score23) = engine.getCurrentEnergyState(Chronotype.BIMODAL_NOCTURNAL, LocalTime.of(23, 0))
        assertTrue("Night score at 23:00 should be >= 0.70, was $score23", score23 >= 0.70f)
    }

    @Test
    fun bimodalNocturnal_morningRestPeriodIsLow() {
        // At 07:00 AM, energy should be low / wind down for sleep recovery
        val (zone, score) = engine.getCurrentEnergyState(Chronotype.BIMODAL_NOCTURNAL, LocalTime.of(7, 0))
        assertTrue("Morning rest score at 07:00 should be <= 0.20, was $score", score <= 0.20f)
        assertEquals(EnergyZone.WIND_DOWN, zone)
    }

    @Test
    fun dailyCurve_produces24Points() {
        val curve = engine.calculateDailyEnergyCurve(Chronotype.BIMODAL_NOCTURNAL)
        assertEquals(24, curve.size)
        assertEquals(0, curve.first().hour)
        assertEquals(23, curve.last().hour)
    }
}
