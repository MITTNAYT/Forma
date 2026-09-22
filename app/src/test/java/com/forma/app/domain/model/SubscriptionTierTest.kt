package com.forma.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionTierTest {

    @Test
    fun `SubscriptionTier fromId parses known tiers correctly`() {
        assertEquals(SubscriptionTier.FREE, SubscriptionTier.fromId("free"))
        assertEquals(SubscriptionTier.FREE, SubscriptionTier.fromId("FREE"))
        assertEquals(SubscriptionTier.MONTHLY_PRO, SubscriptionTier.fromId("monthly_pro"))
        assertEquals(SubscriptionTier.MONTHLY_PRO, SubscriptionTier.fromId("MONTHLY_PRO"))
        assertEquals(SubscriptionTier.LIFETIME_FOUNDER, SubscriptionTier.fromId("lifetime_founder"))
        assertEquals(SubscriptionTier.LIFETIME_FOUNDER, SubscriptionTier.fromId("LIFETIME_FOUNDER"))
    }

    @Test
    fun `SubscriptionTier fromId falls back to FREE for unknown or null ids`() {
        assertEquals(SubscriptionTier.FREE, SubscriptionTier.fromId(null))
        assertEquals(SubscriptionTier.FREE, SubscriptionTier.fromId(""))
        assertEquals(SubscriptionTier.FREE, SubscriptionTier.fromId("some_random_tier"))
    }

    @Test
    fun `isProAccess returns true only for pro tiers`() {
        assertFalse(SubscriptionTier.FREE.isProAccess)
        assertTrue(SubscriptionTier.MONTHLY_PRO.isProAccess)
        assertTrue(SubscriptionTier.LIFETIME_FOUNDER.isProAccess)
    }

    @Test
    fun `isFounder returns true only for lifetime founder tier`() {
        assertFalse(SubscriptionTier.FREE.isFounder)
        assertFalse(SubscriptionTier.MONTHLY_PRO.isFounder)
        assertTrue(SubscriptionTier.LIFETIME_FOUNDER.isFounder)
    }

    @Test
    fun `Monthly Pro includes 7-day free trial`() {
        assertEquals(7, SubscriptionTier.MONTHLY_PRO.trialDays)
        assertEquals(0, SubscriptionTier.FREE.trialDays)
        assertEquals(0, SubscriptionTier.LIFETIME_FOUNDER.trialDays)
    }
}
