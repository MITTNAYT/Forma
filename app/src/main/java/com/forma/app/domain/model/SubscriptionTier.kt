package com.forma.app.domain.model

/**
 * SubscriptionTier defines the 3-tier membership levels for Forma:
 * 1. FREE (Sanctuary Explorer)
 * 2. MONTHLY_PRO (Forma Pro - $4.99/month with 7-Day Free Trial)
 * 3. LIFETIME_FOUNDER (Forma Founder - $49.99 one-time payment)
 */
enum class SubscriptionTier(
    val id: String,
    val title: String,
    val priceDisplay: String,
    val priceSubtext: String,
    val badge: String?,
    val isHighlighted: Boolean = false,
    val trialDays: Int = 0
) {
    FREE(
        id = "free",
        title = "Sanctuary Explorer",
        priceDisplay = "$0",
        priceSubtext = "Free Forever · 3 Habits Max",
        badge = "STARTER",
        isHighlighted = false,
        trialDays = 0
    ),
    MONTHLY_PRO(
        id = "monthly_pro",
        title = "Forma Pro",
        priceDisplay = "$4.99",
        priceSubtext = "per month · 7-day free trial",
        badge = "POPULAR",
        isHighlighted = true,
        trialDays = 7
    ),
    LIFETIME_FOUNDER(
        id = "lifetime_founder",
        title = "Master Architect",
        priceDisplay = "$49.99",
        priceSubtext = "one-time payment · lifetime sovereign guild access",
        badge = "LIFETIME",
        isHighlighted = false,
        trialDays = 0
    );

    val isProAccess: Boolean
        get() = this == MONTHLY_PRO || this == LIFETIME_FOUNDER

    val isFounder: Boolean
        get() = this == LIFETIME_FOUNDER

    companion object {
        fun fromId(id: String?): SubscriptionTier {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FREE
        }
    }
}

data class TierFeatureComparison(
    val featureName: String,
    val category: String,
    val freeValue: String,
    val proValue: String,
    val founderValue: String,
    val isHighlighted: Boolean = false
)
