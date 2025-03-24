package com.khrd.pingapp.di_test

import android.content.Context
import com.khrd.pingapp.utils.passwordValidation.PasswordValidationUseCase
import com.khrd.pingapp.utils.viewstate.AppViewState
import com.khrd.pingapp.di.UtilsModule
import com.khrd.pingapp.utils.DialogUtils
import com.khrd.pingapp.utils.ToastUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [UtilsModule::class])
class UtilsTestModule {
    @Singleton
    @Provides
    fun providePasswordValidationUseCase(): PasswordValidationUseCase = mock(PasswordValidationUseCase::class.java)

    @Singleton
    @Provides
    fun provideToastUtils(@ApplicationContext context: Context): ToastUtils = mock(ToastUtils::class.java)

    @Singleton
    @Provides
    fun provideAppViewState(): AppViewState = mock(AppViewState::class.java)

    @Singleton
    @Provides
    fun provideDialogUtils(): DialogUtils = mock(DialogUtils::class.java)
}