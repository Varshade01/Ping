package com.khrd.pingapp.di

import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.usecases.RemoveFcmTokenUseCase
import com.khrd.pingapp.homescreen.usecases.RemoveFcmTokenUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.UpdateFcmTokenUseCase
import com.khrd.pingapp.homescreen.usecases.UpdateFcmTokenUseCaseImpl
import com.khrd.pingapp.repository.users.UsersRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
class FcmModule {
    @Provides
    fun provideUpdateFcmTokenUseCase(
        userRepository: UsersRepository,
        dataStoreManager: DataStoreManager,
        fireBaseAuth: FirebaseAuthAPI,
        @IoCoroutineScope coroutineScope: CoroutineScope
    ): UpdateFcmTokenUseCase = UpdateFcmTokenUseCaseImpl(
        userRepository,
        dataStoreManager,
        fireBaseAuth,
        coroutineScope
    )

    @Provides
    fun provideRemoveFcmTokenUseCase(
        userRepository: UsersRepository,
        dataStoreManager: DataStoreManager,
        fireBaseAuth: FirebaseAuthAPI,
        @IoCoroutineScope coroutineScope: CoroutineScope
    ): RemoveFcmTokenUseCase = RemoveFcmTokenUseCaseImpl(
        userRepository,
        dataStoreManager,
        fireBaseAuth,
        coroutineScope
    )
}