package com.forma.app.domain.repository

import com.forma.app.domain.model.SubscriptionTier
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    /**
     * Observable Pro subscription status (true if Monthly Pro or Lifetime Founder).
     */
    val isPro: Flow<Boolean>

    /**
     * Observable Current Subscription Tier.
     */
    val currentTier: Flow<SubscriptionTier>

    /**
     * Initiate Monthly Pro subscription ($4.99/month with 7-day free trial).
     */
    suspend fun purchaseMonthlyPro(): Result<Boolean>

    /**
     * Initiate Lifetime Founder one-time purchase ($49.99).
     */
    suspend fun purchaseLifetimeFounder(): Result<Boolean>

    /**
     * Initiate Pro purchase (backwards compatibility).
     */
    suspend fun purchasePro(): Result<Boolean>

    /**
     * Restore purchases from Google Play / Clerk account.
     */
    suspend fun restorePurchases(): Result<Boolean>

    /**
     * Set subscription tier directly (and persist).
     */
    suspend fun setSubscriptionTier(tier: SubscriptionTier)

    /**
     * For debugging / legacy compatibility: directly set pro status.
     */
    suspend fun setProStatus(isPro: Boolean)

    /**
     * Launch official Google Play billing checkout flow on an Activity.
     */
    fun launchBillingFlow(activity: android.app.Activity, tier: SubscriptionTier): Result<Boolean>
}
