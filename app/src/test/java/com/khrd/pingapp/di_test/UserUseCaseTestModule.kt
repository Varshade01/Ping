package com.khrd.pingapp.di_test

import com.khrd.pingapp.di.UserModule
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.usecases.GetUserUseCase
import com.khrd.pingapp.homescreen.usecases.UpdateFcmTokenUseCase
import com.khrd.pingapp.login.LoginUseCase
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.useradministration.usecases.CreateUserUseCase
import com.khrd.pingapp.useradministration.usecases.GetUsersByIdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [UserModule::class])
class UserUseCaseTestModule {

    @Singleton
    @Provides
    fun provideCreateUserUseCase(): CreateUserUseCase = mock(CreateUserUseCase::class.java)

    @Singleton
    @Provides
    fun provideGetUsersByIdUseCase(): GetUsersByIdUseCase = mock(GetUsersByIdUseCase::class.java)

    @Singleton
    @Provides
    fun provideUpdateFcmTokensUseCase(): UpdateFcmTokenUseCase = mock(UpdateFcmTokenUseCase::class.java)

    @Provides
    fun provideGetUserUseCase(repository: UsersRepository): GetUserUseCase = mock(GetUserUseCase::class.java)

    @Provides
    fun provideLoginUseCase(firebaseAuth: FirebaseAuthAPI, updateFcmTokenUseCase: UpdateFcmTokenUseCase): LoginUseCase =
        mock(LoginUseCase::class.java)

}