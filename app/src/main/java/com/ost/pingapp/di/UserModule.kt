package com.khrd.pingapp.di

import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.CheckMutedItemsUseCase
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.CheckMutedItemsUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.*
import com.khrd.pingapp.login.LoginUseCase
import com.khrd.pingapp.login.LoginUseCaseImpl
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.useradministration.usecases.CreateUserUseCase
import com.khrd.pingapp.useradministration.usecases.CreateUserUseCaseImpl
import com.khrd.pingapp.useradministration.usecases.GetUsersByIdUseCase
import com.khrd.pingapp.useradministration.usecases.GetUsersByIdUseCaseImpl
import com.khrd.pingapp.utils.OnlineManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class UserModule {
    @Provides
    fun provideLoginUseCase(
        firebaseAuth: FirebaseAuthAPI,
        updateFcmTokenUseCase: UpdateFcmTokenUseCase,
        onlineManager: OnlineManager
    ): LoginUseCase =
        LoginUseCaseImpl(firebaseAuth, updateFcmTokenUseCase, onlineManager)

    @Provides
    fun provideCreateUserUseCase(
        repository: UsersRepository,
        updateFcmTokenUseCase: UpdateFcmTokenUseCase,
        onlineManager: OnlineManager
    ): CreateUserUseCase =
        CreateUserUseCaseImpl(repository, updateFcmTokenUseCase, onlineManager)

    @Provides
    fun provideGetUsersByIdUseCase(usersRepository: UsersRepository): GetUsersByIdUseCase = GetUsersByIdUseCaseImpl(usersRepository)

    @Provides
    fun provideGetUserUseCase(repository: UsersRepository): GetUserUseCase = GetUserUseCaseImpl(repository)

    @Provides
    fun provideOnlineHandlerUseCase(repository: UsersRepository, firebaseAuth: FirebaseAuthAPI): OnlineHandlerUseCase =
        OnlineHandlerUseCaseImpl(repository, firebaseAuth)

    @Provides
    fun provideUpdateMuteStateUseCase(repository: UsersRepository): UpdateMuteStateUseCase = UpdateMuteStateUseCaseImpl(repository)

    @Provides
    fun provideHideUserInformationUseCase(repository: UsersRepository): HideUserInformationUseCase =
        HideUserInformationUseCaseImpl(repository)

    @Provides
    fun provideCheckMutedItemsUseCase(
        firebaseAuth: FirebaseAuthAPI,
        getUserUseCase: GetUserUseCase,
    ): CheckMutedItemsUseCase = CheckMutedItemsUseCaseImpl(firebaseAuth, getUserUseCase)
}
