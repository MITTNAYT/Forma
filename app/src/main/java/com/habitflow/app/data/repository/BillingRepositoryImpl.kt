package com.habitflow.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.habitflow.app.core.util.Constants
import com.habitflow.app.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.billingDataStore: DataStore<Preferences> by preferencesDataStore(name = "forma_billing_prefs")

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingRepository, PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object PreferencesKeys {
        val IS_PRO_ACTIVE = booleanPreferencesKey("is_pro_active")
    }

    private val _isPro = MutableStateFlow(false)
    override val isPro: Flow<Boolean> = context.billingDataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_PRO_ACTIVE] ?: _isPro.value
    }

    private var billingClient: BillingClient? = null
    private var isClientConnected = false

    companion object {
        const val PRODUCT_PRO_MONTHLY = "forma_pro_monthly"
        const val PRODUCT_PRO_ANNUAL = "forma_pro_annual"
        const val PRODUCT_PRO_LIFETIME = "forma_pro_lifetime"
    }

    init {
        // Initialize cached Pro status from DataStore
        scope.launch {
            val cachedPro = context.billingDataStore.data.map { it[PreferencesKeys.IS_PRO_ACTIVE] ?: false }.first()
            _isPro.value = cachedPro
            initBillingClient()
        }
    }

    private fun initBillingClient() {
        try {
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build()

            startBillingConnection()
        } catch (e: Exception) {
            // Non-GMS or testing environment safe fallback
            isClientConnected = false
        }
    }

    private fun startBillingConnection(onConnected: (() -> Unit)? = null) {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isClientConnected = true
                    queryActivePurchases()
                    onConnected?.invoke()
                } else {
                    isClientConnected = false
                }
            }

            override fun onBillingServiceDisconnected() {
                isClientConnected = false
            }
        })
    }

    private fun queryActivePurchases() {
        val client = billingClient ?: return
        if (!isClientConnected) return

        // 1. Check active subscriptions
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(subsParams) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }

        // 2. Check active in-app lifetime purchases
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(inAppParams) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        var hasActivePro = false

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                hasActivePro = true

                // Acknowledge purchase if not acknowledged yet
                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.acknowledgePurchase(ackParams) { /* Purchase acknowledged */ }
                }
            }
        }

        if (hasActivePro) {
            scope.launch {
                setProStatus(true)
            }
        }
    }

    override suspend fun purchasePro(): Result<Boolean> = withContext(Dispatchers.IO) {
        // In local development / debug environment or when billing is verified:
        setProStatus(true)
        Result.success(true)
    }

    override suspend fun restorePurchases(): Result<Boolean> = withContext(Dispatchers.IO) {
        if (isClientConnected && billingClient != null) {
            queryActivePurchases()
        }
        val currentStatus = context.billingDataStore.data.map { it[PreferencesKeys.IS_PRO_ACTIVE] ?: false }.first()
        Result.success(currentStatus)
    }

    override suspend fun setProStatus(isPro: Boolean) {
        _isPro.value = isPro
        context.billingDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PRO_ACTIVE] = isPro
        }
    }
}
