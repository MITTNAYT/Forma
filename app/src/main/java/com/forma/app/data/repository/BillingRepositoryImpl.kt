package com.forma.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.forma.app.domain.model.SubscriptionTier
import com.forma.app.domain.repository.BillingRepository
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
        val SUBSCRIPTION_TIER = stringPreferencesKey("subscription_tier")
    }

    private val _currentTier = MutableStateFlow(SubscriptionTier.FREE)
    override val currentTier: Flow<SubscriptionTier> = context.billingDataStore.data.map { preferences ->
        val tierId = preferences[PreferencesKeys.SUBSCRIPTION_TIER]
        val legacyPro = preferences[PreferencesKeys.IS_PRO_ACTIVE] ?: false
        if (tierId != null) {
            SubscriptionTier.fromId(tierId)
        } else if (legacyPro) {
            SubscriptionTier.LIFETIME_FOUNDER
        } else {
            _currentTier.value
        }
    }

    override val isPro: Flow<Boolean> = currentTier.map { it.isProAccess }

    private var billingClient: BillingClient? = null
    private var isClientConnected = false

    companion object {
        const val PRODUCT_PRO_MONTHLY = "forma_pro_monthly"
        const val PRODUCT_PRO_LIFETIME = "forma_pro_lifetime"
    }

    init {
        scope.launch {
            val cachedTier = context.billingDataStore.data.map { prefs ->
                val id = prefs[PreferencesKeys.SUBSCRIPTION_TIER]
                val pro = prefs[PreferencesKeys.IS_PRO_ACTIVE] ?: false
                if (id != null) SubscriptionTier.fromId(id)
                else if (pro) SubscriptionTier.LIFETIME_FOUNDER
                else SubscriptionTier.FREE
            }.first()
            _currentTier.value = cachedTier
            initBillingClient()
        }
    }

    private fun initBillingClient() {
        try {
            val pendingParams = com.android.billingclient.api.PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()

            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(pendingParams)
                .build()

            startBillingConnection()
        } catch (e: Exception) {
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

        // 1. Check active subscriptions (Monthly Pro)
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        client.queryPurchasesAsync(subsParams) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases, isSubscription = true)
            }
        }

        // 2. Check active in-app lifetime purchases (Lifetime Founder)
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(inAppParams) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases, isSubscription = false)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases, isSubscription = false)
        }
    }

    private fun handlePurchases(purchases: List<Purchase>, isSubscription: Boolean = false) {
        var resolvedTier: SubscriptionTier? = null

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (purchase.products.contains(PRODUCT_PRO_MONTHLY) || isSubscription) {
                    resolvedTier = SubscriptionTier.MONTHLY_PRO
                } else if (purchase.products.contains(PRODUCT_PRO_LIFETIME)) {
                    resolvedTier = SubscriptionTier.LIFETIME_FOUNDER
                } else {
                    resolvedTier = SubscriptionTier.MONTHLY_PRO
                }

                if (!purchase.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient?.acknowledgePurchase(ackParams) { }
                }
            }
        }

        if (resolvedTier != null) {
            val finalTier = resolvedTier
            scope.launch {
                setSubscriptionTier(finalTier)
            }
        }
    }

    override suspend fun purchaseMonthlyPro(): Result<Boolean> = withContext(Dispatchers.IO) {
        setSubscriptionTier(SubscriptionTier.MONTHLY_PRO)
        Result.success(true)
    }

    override suspend fun purchaseLifetimeFounder(): Result<Boolean> = withContext(Dispatchers.IO) {
        setSubscriptionTier(SubscriptionTier.LIFETIME_FOUNDER)
        Result.success(true)
    }

    override suspend fun purchasePro(): Result<Boolean> = purchaseLifetimeFounder()

    override suspend fun restorePurchases(): Result<Boolean> = withContext(Dispatchers.IO) {
        if (isClientConnected && billingClient != null) {
            queryActivePurchases()
        }
        val current = currentTier.first()
        Result.success(current.isProAccess)
    }

    override suspend fun setSubscriptionTier(tier: SubscriptionTier) {
        _currentTier.value = tier
        context.billingDataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBSCRIPTION_TIER] = tier.id
            preferences[PreferencesKeys.IS_PRO_ACTIVE] = tier.isProAccess
        }
    }

    override suspend fun setProStatus(isPro: Boolean) {
        val targetTier = if (isPro) SubscriptionTier.LIFETIME_FOUNDER else SubscriptionTier.FREE
        setSubscriptionTier(targetTier)
    }
}
