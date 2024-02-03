package com.adentech.recovery.core.fragments

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.adentech.recovery.BR
import com.adentech.recovery.core.viewmodel.BaseViewModel

abstract class BaseFragment<VM : BaseViewModel, DB : ViewDataBinding> :
    RecoveryBaseVmDbFragment<VM, DB>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.setVariable(BR.viewModel, viewModel)
        onInitDataBinding()
    }

    abstract fun onInitDataBinding()
}