package com.adentech.recovery.ui.scan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.adentech.recovery.BuildConfig
import com.adentech.recovery.R
import com.adentech.recovery.core.activities.BaseActivity
import com.adentech.recovery.databinding.ActivityScanBinding
import com.adentech.recovery.ui.home.HomeActivity
import com.adentech.recovery.ui.result.ImagesActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class ScanActivity : BaseActivity<ScanViewModel, ActivityScanBinding>() {

    private var isFreeScan: Boolean = false
    private var doubleBackToExitPressedOnce = false

    override fun viewModelClass() = ScanViewModel::class.java

    override fun viewDataBindingClass() = ActivityScanBinding::class.java

    override fun onInitDataBinding() {
        handleBackPressed()
        val extras = intent.extras
        if (extras != null) {
            isFreeScan = extras.getBoolean("isFreeScan", false)
        }

        setupProgress()

        viewBinding.backButton.setOnClickListener {
            startActivity(HomeActivity.newIntent(this@ScanActivity)).also {
                finishAffinity()
            }
        }
        viewBinding.startStopButton.setOnClickListener {
            startActivity(HomeActivity.newIntent(this@ScanActivity)).also {
                finishAffinity()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupProgress() {
        viewBinding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            for (i in 1..100) {
                if (BuildConfig.DEBUG) {
                    delay(1)
                } else {
                    if (isFreeScan) {
                        delay(200)
                    } else {
                        delay(500)
                    }
                }

                withContext(Dispatchers.Main) {
                    viewBinding.progressBar.progress = i
                    viewBinding.completedTV.text = "$i%"
                }
            }

            runOnUiThread {
                viewBinding.scanningTV.visibility = View.INVISIBLE
                viewBinding.progressBar.visibility = View.INVISIBLE
            }

            startActivity(ImagesActivity.newIntent(this@ScanActivity))
        }
    }

    private fun handleBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    startActivity(Intent(this@ScanActivity, HomeActivity::class.java))
                }
                doubleBackToExitPressedOnce = true
                Toast.makeText(
                    this@ScanActivity,
                    getString(R.string.exit_message),
                    Toast.LENGTH_SHORT
                ).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        }
        onBackPressedDispatcher.addCallback(this@ScanActivity, callback)
    }
}