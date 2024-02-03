package com.adentech.recovery.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.adentech.recovery.RecoveryApplication
import com.adentech.recovery.common.ArgumentKey
import com.adentech.recovery.common.RemoteConfigUtils
import com.adentech.recovery.core.activities.BaseActivity
import com.adentech.recovery.databinding.ActivityHomeBinding
import com.adentech.recovery.ui.onboard.OnboardActivity
import com.adentech.recovery.ui.result.ImagesActivity
import com.adentech.recovery.ui.scan.FreeScanActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<MainViewModel, ActivityHomeBinding>() {

    private var isSplashDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            isSplashDone
        }
    }

    override fun viewModelClass() = MainViewModel::class.java

    override fun viewDataBindingClass() = ActivityHomeBinding::class.java

    override fun onInitDataBinding() {
        val isFirstLaunch = viewModel.preferences.getFirstTimeLaunch()
        val isUserPremium = viewModel.preferences.getIsUserPremium()
        if (isFirstLaunch) {
            isSplashDone = true
            launchOnboard()
        } else {
            if (RecoveryApplication.hasSubscription || isUserPremium) {
                isSplashDone = true
                startActivity(Intent(ImagesActivity.newIntent(this@HomeActivity)))
                finish()
            } else {
                isSplashDone = true
                val intent = Intent(this@HomeActivity, FreeScanActivity::class.java)
                startActivity(intent)
                finish()
           }
        }
    }

    private fun launchOnboard() {
        startActivity(OnboardActivity.newIntent(this)).also {
            finish()
        }
    }

    companion object {
        fun newIntent(context: Context, returnScreen: String? = null) =
            Intent(context, HomeActivity::class.java).apply {
                putExtra(ArgumentKey.HOME_SCREEN, returnScreen)
            }
    }
}