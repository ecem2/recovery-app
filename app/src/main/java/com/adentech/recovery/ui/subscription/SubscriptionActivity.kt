package com.adentech.recovery.ui.subscription

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.adentech.recovery.BuildConfig
import com.adentech.recovery.BuildConfig.MONTHLY_PREMIUM
import com.adentech.recovery.BuildConfig.WEEKLY_PREMIUM
import com.adentech.recovery.BuildConfig.YEARLY_PREMIUM
import com.adentech.recovery.R
import com.adentech.recovery.adapter.ViewPagerAdapter
import com.adentech.recovery.common.ArgumentKey
import com.adentech.recovery.common.Constants
import com.adentech.recovery.common.Constants.FROM_SUBSCRIPTION
import com.adentech.recovery.common.RemoteConfigUtils
import com.adentech.recovery.core.activities.BaseActivity
import com.adentech.recovery.data.billing.MainState
import com.adentech.recovery.databinding.ActivitySubscriptionBinding
import com.adentech.recovery.extensions.observe
import com.adentech.recovery.fbevent.FBEventManager
import com.adentech.recovery.ui.home.HomeActivity
import com.adentech.recovery.ui.home.MainViewModel
import com.adentech.recovery.ui.result.deleted.DeletedImagesFragment
import com.adentech.recovery.ui.scan.FreeScanActivity
import com.adentech.recovery.viewPager.SetPagerDummy
import com.android.billingclient.api.Purchase
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubscriptionActivity : BaseActivity<MainViewModel, ActivitySubscriptionBinding>() {

    private var isFromFreeScan = false
    private var selectedSub: String? = null
    private var weeklyPrice: String? = null
    private var monthlyPrice: String? = null
    private var yearlyPrice: String? = null
    private var productsForSale: MainState? = null
    private var currentPurchases: List<Purchase> = listOf()
    private var subsDone: Boolean = false
    private var mInterstitialAd: InterstitialAd? = null
    private var errorMessage: String? = null
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun viewModelClass() = MainViewModel::class.java

    override fun viewDataBindingClass() = ActivitySubscriptionBinding::class.java

    override fun onInitDataBinding() {
        navigateBack()
        setData()
        clickListeners()
        MobileAds.initialize(this@SubscriptionActivity) {
            loadInterAd()
        }
        setupRemoteConfig()
        isFromFreeScan = intent.getBooleanExtra(Constants.IS_FREE_SCAN, false)
        observe(viewModel.billingConnectionState, ::onBillingConnected)
        observe(viewModel.subscriptionType, ::onSubscriptionTypeChanged)
        selectedSub = YEARLY_PREMIUM
    }

    private fun clickListeners() {
        viewBinding.apply {
            closeButton.setOnClickListener {
                FBEventManager().logEvent("subs_screen_closed")
                if (isFromFreeScan) {
                    if (mInterstitialAd == null) {
                        finishAffinity()
                    } else {
                        showInterAds()
                    }
                }
            }

            monthlyButton.setOnClickListener {
                selectedSub = MONTHLY_PREMIUM
                monthlyButton.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.subscription_toggle_selected_bg
                )
                yearlyButton.background = null
                monthlyButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black_text
                    )
                )
                yearlyButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.white
                    )
                )
            }

            yearlyButton.setOnClickListener {
                selectedSub = YEARLY_PREMIUM
                monthlyButton.background = null
                yearlyButton.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.subscription_toggle_selected_bg
                )
                monthlyButton.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
                yearlyButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black_text
                    )
                )

            }

            termsOfUse.setOnClickListener {
                val intent = Intent(this@SubscriptionActivity, FullscreenActivity::class.java)
                intent.putExtra("title", "TERMS")
                startActivity(intent)
            }

            privacyPolicy.setOnClickListener {
                val intent = Intent(this@SubscriptionActivity, FullscreenActivity::class.java)
                intent.putExtra("title", "PRIVACY")
                startActivity(intent)
            }
            monthlyButton.setOnClickListener {
                FBEventManager().logEvent("selected_subscription", "sub", selectedSub.toString())
                when (selectedSub) {
                    MONTHLY_PREMIUM -> {
                        buyMonthlyPremium()
                    }
                }
            }
            yearlyButton.setOnClickListener {
                FBEventManager().logEvent("selected_subscription", "sub", selectedSub.toString())
                when (selectedSub) {
                    YEARLY_PREMIUM -> {
                        buyYearlyPremium()
                    }
                }
            }

        }
    }
    private fun navigateBack() {
        supportFragmentManager.popBackStack("PreviousFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val parentIntent = NavUtils.getParentActivityIntent(this)
                parentIntent?.flags =
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(parentIntent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setData() {
        viewPagerAdapter = ViewPagerAdapter()

        viewPagerAdapter.setPagerAdapter(SetPagerDummy.setDataPager())
        viewBinding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(viewBinding.intoTabLayout, viewBinding.viewPager) { tab, position -> }.attach()

    }

    private fun setupRemoteConfig() {
        if (RemoteConfigUtils.checkMonthOnly().toString() == "true") {
            viewBinding.apply {
                selectedSub = MONTHLY_PREMIUM
                monthlyButton.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.subscription_toggle_selected_bg
                )
                monthlyButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black_text
                    )
                )
                yearlyButton.text = yearlyPrice
               // subscriptionName.text = getString(R.string.weekly_title)
            }
        } else if (RemoteConfigUtils.checkYearlyOnly().toString() == "true") {
            selectedSub = YEARLY_PREMIUM
            viewBinding.apply {
                yearlyButton.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.subscription_toggle_selected_bg
                )
                yearlyButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black_text
                    )
                )
                monthlyButton.text = monthlyPrice
                //subscriptionName.text = getString(R.string.yearly_title)
            }
        }
    }
    private fun onSubscriptionTypeChanged(subscriptionType: MainViewModel.SubscriptionType) {
        if (subscriptionType != MainViewModel.SubscriptionType.NOT_SUBSCRIBED) {
            val intent = Intent(this@SubscriptionActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
//    override fun onBackPressed() {
//        if (isFromFreeScan) {
//            if (mInterstitialAd == null) {
//                finish()
//            } else {
//                showInterAds()
//            }
//        }
//    }

    private fun onBillingConnected(value: Boolean) {
        FBEventManager().logEvent("subs_screen_billing", "connection", value.toString())
        if (!value) {
            // When false connection to the billing library is not established yet.
        } else {
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.productsForSaleFlows.collectLatest {
                        productsForSale = it
                        showProducts()
                    }

                    viewModel.currentPurchasesFlow.collectLatest {
                        currentPurchases = it
                    }

                    viewModel.isAcknowledged.collectLatest {
                        FBEventManager().logEvent(
                            "subs_screen_is_acknowledged",
                            "isAcknowledged",
                            it.toString()
                        )
                        subsDone = it
                    }

                    viewModel.errorMessage.collectLatest {
                        errorMessage = it
                        if (it.isNotBlank() && it != "") {
                            Toast.makeText(
                                this@SubscriptionActivity,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        if (currentPurchases.isNotEmpty() || subsDone) {
            FBEventManager().logEvent(
                "subs_done",
                "subs_done",
                "true"
            )
            val intent = Intent(this@SubscriptionActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


    private fun showProducts() {
//        productsForSale?.basicWeeklyDetails?.subscriptionOfferDetails?.forEach {
//            it.pricingPhases.pricingPhaseList.forEach { it1 ->
//                weeklyPrice = it1.formattedPrice
//            }
//        }

        productsForSale?.basicMonthlyDetails?.subscriptionOfferDetails?.forEach {
            it.pricingPhases.pricingPhaseList.forEach { it1 ->
                monthlyPrice = it1.formattedPrice
            }
        }

        productsForSale?.basicYearlyDetails?.subscriptionOfferDetails?.forEach {
            it.pricingPhases.pricingPhaseList.forEach { prodDetail ->
                yearlyPrice = prodDetail.formattedPrice
            }
        }

//        runOnUiThread {
//            when (selectedSub) {
//                YEARLY_PREMIUM -> {
//                    viewBinding.subscriptionPriceTV.text = yearlyPrice
//                    viewBinding.notFreeTrial.text =
//                        getString(R.string.yearly_free_trial_period).format(yearlyPrice)
//                }
//
//                MONTHLY_PREMIUM -> {
//                    viewBinding.subscriptionPriceTV.text = monthlyPrice
//                    viewBinding.notFreeTrial.text =
//                        getString(R.string.monthly_free_trial_period).format(monthlyPrice)
//                }
//
//                WEEKLY_PREMIUM -> {
//                    viewBinding.subscriptionPriceTV.text = weeklyPrice
//                    viewBinding.notFreeTrial.text =
//                        getString(R.string.weekly_free_trial_period).format(weeklyPrice)
//                }
//            }
//        }
    }

    private fun buyWeeklyPremium() {
        productsForSale?.basicWeeklyDetails?.let {
            viewModel.buy(
                productDetails = it,
                activity = this@SubscriptionActivity
            )
        }
    }

    private fun buyMonthlyPremium() {
        productsForSale?.basicMonthlyDetails?.let {
            viewModel.buy(
                productDetails = it,
                activity = this@SubscriptionActivity
            )
        }
    }

    private fun buyYearlyPremium() {
        productsForSale?.basicYearlyDetails?.let {
            viewModel.buy(
                productDetails = it,
                activity = this@SubscriptionActivity
            )
        }
    }

    private fun showInterAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    mInterstitialAd = null
                    loadInterAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    mInterstitialAd = null

                    val resultIntent = Intent()
                    resultIntent.putExtra(FROM_SUBSCRIPTION, true)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }
            mInterstitialAd?.show(this@SubscriptionActivity)
        } else {
            return
        }
    }

    private fun loadInterAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            BuildConfig.INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
    }

    companion object {
        fun newIntent(context: Context, returnScreen: String? = null) =
            Intent(context, SubscriptionActivity::class.java).apply {
                putExtra(ArgumentKey.SUBSCRIPTION_SCREEN, returnScreen)
            }
        const val SOURCE_FRAGMENT = "source_fragment"
        fun newIntent(context: Context, returnScreen: String? = null, sourceFragment: String? = null) =
            Intent(context, SubscriptionActivity::class.java).apply {
                putExtra(ArgumentKey.SUBSCRIPTION_SCREEN, returnScreen)
                putExtra(SOURCE_FRAGMENT, sourceFragment)
            }
    }
}