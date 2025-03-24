package com.khrd.pingapp.di

import android.content.Context
import androidx.room.Room
import com.khrd.pingapp.data.database.DatabaseRoom

import com.khrd.pingapp.data.database.groups.GroupDao
import com.khrd.pingapp.data.database.pings.PingDao
import com.khrd.pingapp.data.database.users.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RoomModule {

    @Singleton
    @Provides
    fun provideRoomDatabase(
        @ApplicationContext applicationContext: Context
    ): DatabaseRoom = Room.databaseBuilder(
        applicationContext,
        DatabaseRoom::class.java, "inTouch-database"
    ).build()

    @Singleton
    @Provides
    fun provideGroupDao(appDatabase: DatabaseRoom): GroupDao = appDatabase.groupDao()

    @Singleton
    @Provides
    fun providePingDao(roomDatabase: DatabaseRoom): PingDao = roomDatabase.pingDao()

    @Singleton
    @Provides
    fun provideUsersDao(roomDatabase: DatabaseRoom): UserDao = roomDatabase.userDao()
}

