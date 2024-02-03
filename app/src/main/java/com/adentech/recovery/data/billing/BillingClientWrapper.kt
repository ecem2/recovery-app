package com.adentech.recovery.data.billing

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adentech.recovery.BuildConfig
import com.adentech.recovery.R
import com.adentech.recovery.RecoveryApplication
import com.adentech.recovery.data.preferences.Preferences
import com.adentech.recovery.fbevent.FBEventManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingClientWrapper(
    @ApplicationContext
    val context: Context,
    private val preferences: Preferences
) : PurchasesUpdatedListener {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _productWithProductDetails =
        MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productWithProductDetails = _productWithProductDetails.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(listOf())
    val purchases = _purchases.asStateFlow()

    private val _isNewPurchaseAcknowledged = MutableStateFlow(value = false)
    val isNewPurchaseAcknowledged = _isNewPurchaseAcknowledged.asStateFlow()

    private val _billingErrorMessage = MutableStateFlow(value = "")
    val billingErrorMessage = _billingErrorMessage.asStateFlow()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun startBillingConnection(billingConnectionState: MutableLiveData<Boolean>) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    FBEventManager().logEvent("bc_service_connected")
                    queryPurchases()
                    queryProductDetails()
                    billingConnectionState.postValue(true)
                }
            }

            override fun onBillingServiceDisconnected() {
                FBEventManager().logEvent("bc_service_disconnected")
                retryBillingServiceConnection()
            }
        })
    }

    private fun retryBillingServiceConnection() {
        val maxTries = 3
        var tries = 1
        var isConnectionEstablished = false
        do {
            try {
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingServiceDisconnected() {

                    }

                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            isConnectionEstablished = true
                        }
                    }
                })
            } catch (e: Exception) {
                tries++
            }
        } while (tries <= maxTries && !isConnectionEstablished)
    }

    private fun checkIsSubscriptionSupported(): Boolean {
        return billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).responseCode == BillingClient.BillingResponseCode.OK
    }

    fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (purchaseList.isNotEmpty()) {
                    FBEventManager().logEvent("purchase_list_is_not_empty")
                    RecoveryApplication.hasSubscription = true
                    preferences.setIsUserPremium(true)
                    _purchases.value = purchaseList
                } else {
                    FBEventManager().logEvent("purchase_list_is_empty")
                    _purchases.value = emptyList()
                    RecoveryApplication.hasSubscription = true
                    preferences.setIsUserPremium(true)
                }
            }
        }
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BuildConfig.WEEKLY_PREMIUM)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BuildConfig.MONTHLY_PREMIUM)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BuildConfig.YEARLY_PREMIUM)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient.queryProductDetailsAsync(
            params
        ) { _: BillingResult?, prodDetailsList: List<ProductDetails> ->
            val newMap: Map<String, ProductDetails> = prodDetailsList.associateBy {
                it.productId
            }
            _productWithProductDetails.value = newMap
        }
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams) {
        billingClient.launchBillingFlow(activity, params)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            FBEventManager().logEvent("purchase_updated_successfully")
            coroutineScope.launch {
                handlePurchase(purchases)
            }
        } else {
            FBEventManager().logEvent("purchase_update_error")
            _billingErrorMessage.value = handleBillingError(billingResult.responseCode)
        }
    }

    private suspend fun handlePurchase(purchase: MutableList<Purchase>?) {
        if (purchase != null) {
            for (pur in purchase) {
                if (pur.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    FBEventManager().logEvent("purchased")
                    if (!pur.isAcknowledged) {
                        FBEventManager().logEvent("purchase_not_acknowledged")
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(pur.purchaseToken)
                        withContext(Dispatchers.IO) {
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) { br->
                                if (br.responseCode == BillingClient.BillingResponseCode.OK) {
                                    FBEventManager().logEvent("acknowledge_br_success")
                                    RecoveryApplication.hasSubscription = false
                                    preferences.setIsUserPremium(false)
                                    _purchases.value = purchase
                                    _isNewPurchaseAcknowledged.value = true
                                } else {
                                    FBEventManager().logEvent(
                                        "acknowledge_fail",
                                        "billing_response",
                                        br.responseCode.toString()
                                    )
                                    _billingErrorMessage.value = handleBillingError(br.responseCode)
                                    RecoveryApplication.hasSubscription = true // Testte premium acmak icin true yap
                                    preferences.setIsUserPremium(true) // Testte premium acmak icin true yap
                                }
                            }
                        }
                    } else {
                        FBEventManager().logEvent("purchase_acknowledged_automatically")
                    }
                }
            }
        }
    }

    fun terminateBillingConnection() {
        billingClient.endConnection()
    }

    private fun handleBillingError(responseCode: Int): String {
        FBEventManager().logEvent("billing_error", "response_code", responseCode.toString())
        val errorMessage: String = when (responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> context.getString(R.string.service_unavailable)
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> context.getString(R.string.error_occured)
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> context.getString(R.string.not_supported)
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> context.getString(R.string.already_owned)
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> context.getString(R.string.do_not_own)
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> context.getString(R.string.item_not_available)
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> context.getString(R.string.service_disconnected)
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> context.getString(R.string.service_timed_out)
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> context.getString(R.string.service_unavailable)
            BillingClient.BillingResponseCode.USER_CANCELED -> context.getString(R.string.purchase_cancelled)
            else -> context.getString(R.string.unknown_error)
        }
        return errorMessage
    }

    companion object {
        private const val TAG = "BillingClientWrapper"
    }
}