package com.khrd.pingapp.repository.pings

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.data.pings.PingData
import com.khrd.pingapp.data.pings.PingState
import kotlinx.coroutines.flow.StateFlow

interface PingsRepository {

    fun createPing(pingData: PingData, callback: (PingState) -> Unit)

    fun deleteScheduledPing(id: String, callback: (PingState) -> Unit)
    fun loadMoreSentPings(fromUserId: String?)
    fun getSentPings(fromUserId: String?): StateFlow<List<DatabasePing>?>
    fun getScheduledPings(fromUserId: String?, dataLoadFlag: DataLoadFlag = DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE): StateFlow<List<DatabasePing>?>
    fun getScheduledPingsWithPagination(fromUserId: String, dataLoadFlag: DataLoadFlag)
    fun loadMoreReceivedPings(userId: String?)
    fun loadReceivedPings(userId: String?): StateFlow<ReceivedPingsData?>
    fun loadMoreScheduledPings(fromUserId: String?)
    fun changePingSeenStatus(pingId: String)

    fun clearReceivedPingsCache()
    fun clearSentPingsCache()
    fun clearScheduledPingsCache()

    //Cache ping which was sent in offline mode
    fun cacheSentPingOffline(offlinePing: DatabasePing)
    //Removing scheduled ping from database
    fun removeScheduledPingFromDB(scheduledPingId: String)
}