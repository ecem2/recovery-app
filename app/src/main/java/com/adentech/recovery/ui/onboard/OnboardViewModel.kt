package com.adentech.recovery.ui.onboard

import com.adentech.recovery.core.viewmodel.BaseViewModel
import com.adentech.recovery.data.preferences.Preferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardViewModel @Inject constructor(
    val preferences: Preferences
): BaseViewModel()