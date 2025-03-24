package com.khrd.pingapp.di_test

import com.khrd.pingapp.di.FcmModule
import com.khrd.pingapp.homescreen.usecases.RemoveFcmTokenUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [FcmModule::class])
class FcmTestModule {
    @Provides
    fun provideRemoveFcmTokenUseCase(): RemoveFcmTokenUseCase = mock(RemoveFcmTokenUseCase::class.java)
}