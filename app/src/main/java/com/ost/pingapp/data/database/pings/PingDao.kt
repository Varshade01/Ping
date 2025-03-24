package com.khrd.pingapp.data.database.pings

import androidx.room.*

@Dao
interface PingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReceivedPing(ping: ReceivedPingEntity)

    @Query("SELECT * FROM ReceivedPings_table WHERE receivedPingEntityId =:pingId")
    fun getReceivedPingFromDb(pingId: String): ReceivedPingEntity

    @Query("SELECT * FROM ReceivedPings_table")
    fun getReceivedPingsFromDb(): List<ReceivedPingEntity>

    @Query("DELETE FROM ReceivedPings_table")
    fun deleteAllReceivedPings()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSentPing(ping: SentPingEntity)

    @Query("SELECT * FROM SentPings_table")
    fun getSentPingsFromDb(): List<SentPingEntity>

    @Query("DELETE FROM SentPings_table")
    fun deleteAllSentPings()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertScheduledPing(ping: ScheduledPingEntity)

    @Query("SELECT * FROM ScheduledPings_table")
    fun getScheduledPingsFromDb(): List<ScheduledPingEntity>

    @Query("DELETE FROM ScheduledPings_table")
    fun deleteAllScheduledPings()

    @Query("DELETE FROM ScheduledPings_table WHERE scheduledPingEntityId =:pingId")
    fun deleteScheduledPing(pingId: String)

}