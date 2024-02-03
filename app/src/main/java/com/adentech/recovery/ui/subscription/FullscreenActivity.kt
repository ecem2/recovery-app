package com.adentech.recovery.ui.subscription

import android.annotation.SuppressLint
import android.webkit.WebViewClient
import com.adentech.recovery.BuildConfig.PRIVACY_POLICY_LINK
import com.adentech.recovery.BuildConfig.TERMS_OF_USE_LINK
import com.adentech.recovery.core.activities.BaseActivity
import com.adentech.recovery.databinding.ActivityFullscreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FullscreenActivity : BaseActivity<SubscriptionViewModel, ActivityFullscreenBinding>() {

    override fun viewModelClass() = SubscriptionViewModel::class.java

    override fun viewDataBindingClass() = ActivityFullscreenBinding::class.java

    @SuppressLint("SetJavaScriptEnabled")
    override fun onInitDataBinding() {
        val title = intent.getStringExtra("title")
        viewBinding.apply {
            webView.webViewClient = WebViewClient()
            val url: String = if (title == "PRIVACY") {
                PRIVACY_POLICY_LINK
            } else {
                TERMS_OF_USE_LINK
            }
            webView.loadUrl(url)
            webView.settings.javaScriptEnabled = true
            webView.settings.setSupportZoom(true)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewBinding.webView.canGoBack()) {
            viewBinding.webView.goBack()
        }
        super.onBackPressed()
    }
}