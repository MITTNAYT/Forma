package com.habitflow.app.data.repository

import com.habitflow.app.domain.repository.BillingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor() : BillingRepository {

    // Default to false for Freemium model; can be toggled by the user in Settings for testing
    private val _isPro = MutableStateFlow(false)
    override val isPro: Flow<Boolean> = _isPro.asStateFlow()

    override suspend fun purchasePro(): Result<Boolean> {
        // TODO: Wire real Google Play Billing client:
        // 1. Initialize BillingClient with PurchasesUpdatedListener
        // 2. Query product details for "habitflow_pro_monthly" / "habitflow_pro_lifetime"
        // 3. Launch billing flow with BillingFlowParams
        // 4. Verify purchase token on backend / acknowledge purchase
        
        // Mocking successful purchase for demo / testing:
        _isPro.value = true
        return Result.success(true)
    }

    override suspend fun restorePurchases(): Result<Boolean> {
        // TODO: Wire real Google Play Billing client queryPurchasesAsync()
        return Result.success(_isPro.value)
    }

    override suspend fun setProStatus(isPro: Boolean) {
        _isPro.value = isPro
    }
}
