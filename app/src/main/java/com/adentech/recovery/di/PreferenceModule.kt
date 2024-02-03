package com.adentech.recovery.di

import com.adentech.recovery.data.preferences.Preferences
import com.adentech.recovery.data.preferences.RecoveryPreferenceManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferenceModule {

    @Binds
    abstract fun providePreferences(preferences: RecoveryPreferenceManager): Preferences
}