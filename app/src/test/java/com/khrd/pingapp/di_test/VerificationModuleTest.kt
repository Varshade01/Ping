package com.khrd.pingapp.di_test

import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.usecases.RemoveFcmTokenUseCase
import com.khrd.pingapp.registration.verification.isEmailVerified.IsEmailVerifiedUseCase
import com.khrd.pingapp.registration.verification.isEmailVerified.VerificationModule
import com.khrd.pingapp.registration.verification.logout.LogoutUseCase
import com.khrd.pingapp.registration.verification.sendEmail.SendConfirmationEmailUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [VerificationModule::class])
class VerificationModuleTest {
    @Provides
    fun bindVerificationUseCase(firebaseAuthAPI: FirebaseAuthAPI): IsEmailVerifiedUseCase = mock(IsEmailVerifiedUseCase::class.java)

    @Provides
    fun bindLogoutUseCase(
        firebaseAuthAPI: FirebaseAuthAPI,
        removeFcmTokenUseCase: RemoveFcmTokenUseCase
    ): LogoutUseCase = mock(LogoutUseCase::class.java)

    @Provides
    fun bindSendConfirmationEmailUseCase(
        firebaseAuthAPI: FirebaseAuthAPI
    ): SendConfirmationEmailUseCase = mock(SendConfirmationEmailUseCase::class.java)
}