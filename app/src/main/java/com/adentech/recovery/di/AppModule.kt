package com.adentech.recovery.di

import android.content.Context
import com.adentech.recovery.data.repository.ImageRepository
import com.adentech.recovery.data.repository.ImageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun injectNormalRepo(@ApplicationContext context: Context) = ImageRepositoryImpl(context) as ImageRepository

}