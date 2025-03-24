package com.khrd.pingapp.registration.verification.isEmailVerified

import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.usecases.RemoveFcmTokenUseCase
import com.khrd.pingapp.registration.verification.logout.LogoutUseCase
import com.khrd.pingapp.registration.verification.logout.LogoutUseCaseImpl
import com.khrd.pingapp.registration.verification.sendEmail.SendConfirmationEmailUseCase
import com.khrd.pingapp.registration.verification.sendEmail.SendConfirmationEmailUseCaseImpl
import com.khrd.pingapp.utils.OnlineManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class VerificationModule {

    @Provides
    fun bindVerificationUseCase(firebaseAuthAPI: FirebaseAuthAPI): IsEmailVerifiedUseCase = IsEmailVerifiedUseCaseImpl(firebaseAuthAPI)

    @Provides
    fun bindLogoutUseCase(
        firebaseAuthAPI: FirebaseAuthAPI,
        removeFcmTokenUseCase: RemoveFcmTokenUseCase,
        onlineManager: OnlineManager
    ): LogoutUseCase = LogoutUseCaseImpl(firebaseAuthAPI, removeFcmTokenUseCase, onlineManager)

    @Provides
    fun bindSendConfirmationEmailUseCase(
        firebaseAuthAPI: FirebaseAuthAPI
    ): SendConfirmationEmailUseCase = SendConfirmationEmailUseCaseImpl(firebaseAuthAPI)
}