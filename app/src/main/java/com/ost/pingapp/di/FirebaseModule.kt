package com.khrd.pingapp.di

import com.khrd.pingapp.data.groups.GroupsDataSource
import com.khrd.pingapp.data.pings.PingsDataSource
import com.khrd.pingapp.data.users.UsersDataSource
import com.khrd.pingapp.firebase.FirebaseDynamicLink
import com.khrd.pingapp.firebase.FirebaseDynamicLinkAPI
import com.khrd.pingapp.firebase.authentication.FirebaseAuth
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.firebase.connection.FirebaseConnectionStatus
import com.khrd.pingapp.firebase.database.groups.FirebaseGroupsDataSource
import com.khrd.pingapp.firebase.database.pings.FirebasePingsDataSource
import com.khrd.pingapp.firebase.database.users.FirebaseUsersDataSource

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FirebaseModule {
    @Singleton
    @Provides
    fun provideFirebase(): FirebaseAuthAPI = FirebaseAuth()

    @Singleton
    @Provides
    fun provideFirebaseUserDatabase(
        firebaseConnectionStatus: ConnectionStatus,
        @IoCoroutineScope coroutineScope: CoroutineScope
    ): UsersDataSource = FirebaseUsersDataSource(firebaseConnectionStatus, coroutineScope)

    @Singleton
    @Provides
    fun provideFirebaseGroupDatabase(firebaseConnectionStatus: ConnectionStatus): GroupsDataSource =
        FirebaseGroupsDataSource(firebaseConnectionStatus)

    @Provides
    fun provideFirebaseDynamicLinkApi(): FirebaseDynamicLinkAPI = FirebaseDynamicLink()

    @Singleton
    @Provides
    fun provideFirebasePingsDataSource(firebaseConnectionStatus: ConnectionStatus): PingsDataSource =
        FirebasePingsDataSource(firebaseConnectionStatus)


    @Singleton
    @Provides
    fun provideConnectionStatus(@IoCoroutineScope ioCoroutineScope: CoroutineScope): ConnectionStatus =
        FirebaseConnectionStatus(ioCoroutineScope)
}