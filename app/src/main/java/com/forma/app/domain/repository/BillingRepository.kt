package com.forma.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    /**
     * Observable Pro subscription status.
     * Free users get false; Pro subscribers get true.
     */
    val isPro: Flow<Boolean>

    /**
     * Initiate Pro purchase (or toggle in mock mode).
     * @return Result with success status.
     */
    suspend fun purchasePro(): Result<Boolean>

    /**
     * Restore purchases from Google Play.
     */
    suspend fun restorePurchases(): Result<Boolean>

    /**
     * For debugging / settings screen testing: directly set pro status.
     */
    suspend fun setProStatus(isPro: Boolean)
}
