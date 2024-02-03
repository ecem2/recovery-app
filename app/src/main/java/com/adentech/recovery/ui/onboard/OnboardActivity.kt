package com.adentech.recovery.ui.onboard

import android.content.Context
import android.content.Intent
import com.adentech.recovery.RecoveryApplication
import com.adentech.recovery.common.ArgumentKey
import com.adentech.recovery.core.activities.BaseActivity
import com.adentech.recovery.databinding.ActivityOnboardBinding
import com.adentech.recovery.ui.home.MainViewModel
import com.adentech.recovery.ui.result.ImagesActivity
import com.adentech.recovery.ui.scan.FreeScanActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardActivity : BaseActivity<MainViewModel, ActivityOnboardBinding>() {

    override fun viewModelClass() = MainViewModel::class.java

    override fun viewDataBindingClass() = ActivityOnboardBinding::class.java

    override fun onInitDataBinding() {
        hideSystemUI()
        viewBinding.continueButton.setOnClickListener {
            viewModel.preferences.setFirstTimeLaunch(false)
            if (RecoveryApplication.hasSubscription) {
                startActivity(ImagesActivity.newIntent(this@OnboardActivity)).also {
                    finishAffinity()
                }
            } else {
                startActivity(FreeScanActivity.newIntent(this@OnboardActivity).also {
                    finishAffinity()
                })
            }
        }
    }

    companion object {
        fun newIntent(context: Context, returnScreen: String? = null) =
            Intent(context, OnboardActivity::class.java).apply {
                putExtra(ArgumentKey.ONBOARD_SCREEN, returnScreen)
            }
    }
}