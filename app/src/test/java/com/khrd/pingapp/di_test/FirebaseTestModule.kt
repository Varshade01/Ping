package com.khrd.pingapp.di_test

import com.khrd.pingapp.data.groups.GroupsDataSource
import com.khrd.pingapp.data.pings.PingsDataSource
import com.khrd.pingapp.data.users.UsersDataSource
import com.khrd.pingapp.di.FirebaseModule
import com.khrd.pingapp.firebase.FirebaseDynamicLinkAPI
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [FirebaseModule::class])
class FirebaseTestModule {
    @Singleton
    @Provides
    fun provideFirebase(): FirebaseAuthAPI = mock(FirebaseAuthAPI::class.java)

    @Singleton
    @Provides
    fun provideFirebaseUserDatabase(): UsersDataSource = mock(UsersDataSource::class.java)

    @Singleton
    @Provides
    fun provideFirebaseGroupDatabase(): GroupsDataSource = mock(GroupsDataSource::class.java)

    @Singleton
    @Provides
    fun provideFirebaseDynamicLinkApi(): FirebaseDynamicLinkAPI = mock(FirebaseDynamicLinkAPI::class.java)

    @Singleton
    @Provides
    fun provideFirebasePingsDataSource(): PingsDataSource = mock(PingsDataSource::class.java)

    @Singleton
    @Provides
    fun provideFirebaseEnableOffline(): FirebaseEnableOfflineMode = mock(FirebaseEnableOfflineMode::class.java)

    @Singleton
    @Provides
    fun provideConnectionStatus(): ConnectionStatus = mock(ConnectionStatus::class.java)

}