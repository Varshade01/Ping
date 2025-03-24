package com.khrd.pingapp.di

import com.khrd.pingapp.utils.deviceIdHelper.DeviceIdHelper
import com.khrd.pingapp.data.database.groups.GroupDao
import com.khrd.pingapp.data.database.groups.GroupMapper
import com.khrd.pingapp.data.database.pings.PingDao
import com.khrd.pingapp.data.database.pings.PingMapper
import com.khrd.pingapp.data.database.users.UserDao
import com.khrd.pingapp.data.database.users.UserMapper
import com.khrd.pingapp.data.groups.GroupsDataSource
import com.khrd.pingapp.data.pings.PingsDataSource
import com.khrd.pingapp.data.users.UsersDataSource
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.repository.groups.GroupRepository
import com.khrd.pingapp.repository.groups.GroupRepositoryImpl
import com.khrd.pingapp.repository.pings.PingsRepository
import com.khrd.pingapp.repository.pings.PingsRepositoryImpl
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.repository.users.UsersRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Singleton
    @Provides
    fun provideUsersRepository(
        dataSource: UsersDataSource,
        userMapper: UserMapper,
        userDao: UserDao,
        firebaseConnectionStatus: ConnectionStatus,
        @IoCoroutineScope ioCoroutineScope: CoroutineScope,
        deviceIdHelper: DeviceIdHelper,
    ): UsersRepository =
        UsersRepositoryImpl(dataSource, userMapper, userDao, firebaseConnectionStatus, ioCoroutineScope, deviceIdHelper)

    @Singleton
    @Provides
    fun provideGroupsRepository(
        dataSource: GroupsDataSource,
        groupDao: GroupDao,
        mapper: GroupMapper,
        firebaseConnectionStatus: ConnectionStatus,
        @IoCoroutineScope ioCoroutineScope: CoroutineScope
    ): GroupRepository = GroupRepositoryImpl(dataSource, groupDao, mapper, firebaseConnectionStatus, ioCoroutineScope)

    @Singleton
    @Provides
    fun providePingsRepository(
        pingDao: PingDao,
        dataSource: PingsDataSource,
        connectionStatus: ConnectionStatus,
        firebaseAuth: FirebaseAuthAPI,
        mapper: PingMapper,
        @IoCoroutineScope ioCoroutineScope: CoroutineScope
    ): PingsRepository = PingsRepositoryImpl(pingDao, dataSource, connectionStatus, firebaseAuth, mapper, ioCoroutineScope)
}