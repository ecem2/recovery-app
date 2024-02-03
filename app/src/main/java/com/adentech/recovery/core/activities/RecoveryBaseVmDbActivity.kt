package com.adentech.recovery.core.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.adentech.recovery.core.common.ViewBindingUtil
import com.adentech.recovery.core.viewmodel.RecoveryBaseViewModel
import com.adentech.recovery.databinding.ActivityRecoveryBaseBinding

abstract class RecoveryBaseVmDbActivity<VM : RecoveryBaseViewModel, DB : ViewDataBinding> :
    RecoveryBaseVmActivity<VM>() {

    protected lateinit var viewBinding: DB

    abstract fun viewDataBindingClass(): Class<DB>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = setResourceViewBinding()
        setContentView(view)
        viewBinding.lifecycleOwner = this
        onInitDataBinding()
    }

    open fun setResourceViewBinding(): View {
        val baseViewBinding = ViewBindingUtil.inflate<ActivityRecoveryBaseBinding>(layoutInflater)
        viewBinding = ViewBindingUtil.inflate(
            layoutInflater,
            baseViewBinding.baseRecoveryContentFrame,
            true,
            viewDataBindingClass()
        )
        return baseViewBinding.root
    }

    abstract fun onInitDataBinding()
}