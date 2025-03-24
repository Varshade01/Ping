package com.khrd.pingapp.data.database.pings

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.khrd.pingapp.data.database.TypeConverter
import com.khrd.pingapp.data.pings.RecurringTime

@Entity(tableName = "ReceivedPings_table")
data class ReceivedPingEntity(
    @PrimaryKey
    val receivedPingEntityId: String,
    val timestamp: Long?,
    @TypeConverters(TypeConverter::class)
    val from: HashMap<String, String> = hashMapOf(),
    @TypeConverters(TypeConverter::class)
    val receivers: HashMap<String, String> = hashMapOf(),
    val message: String,
    @TypeConverters(TypeConverter::class)
    val views: HashMap<String, String> = hashMapOf(),
    val groupId: String?,
    val scheduledTime: Long?,
    val groupFrom: String,
    val recurringTime: RecurringTime
)
