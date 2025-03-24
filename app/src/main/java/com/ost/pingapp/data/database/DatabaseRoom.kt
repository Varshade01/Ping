package com.khrd.pingapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.khrd.pingapp.constants.DbConstants
import com.khrd.pingapp.data.database.groups.GroupDao
import com.khrd.pingapp.data.database.groups.GroupEntity
import com.khrd.pingapp.data.database.pings.*
import com.khrd.pingapp.data.database.users.UserDao
import com.khrd.pingapp.data.database.users.UserEntity

@TypeConverters(TypeConverter::class)
@Database(
    entities = [GroupEntity::class, UserEntity::class, ReceivedPingEntity::class, SentPingEntity::class, ScheduledPingEntity::class],
    version = DbConstants.ROOM_DATABASE_VERSION
)
abstract class DatabaseRoom : RoomDatabase() {

    abstract fun groupDao(): GroupDao

    abstract fun pingDao(): PingDao

    abstract fun userDao(): UserDao
}
