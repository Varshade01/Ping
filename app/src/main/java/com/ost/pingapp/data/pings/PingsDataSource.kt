package com.khrd.pingapp.data.pings

import com.khrd.pingapp.homescreen.listeners.NewPingsListener
import com.khrd.pingapp.homescreen.listeners.NewPingsSentListener
import com.khrd.pingapp.homescreen.listeners.ScheduledPingsListener
import com.khrd.pingapp.homescreen.states.PingsType

interface PingsDataSource {
    fun createPing(pingData: PingData, callback: (PingState) -> Unit)

    fun getScheduledPings(fromUserId: String, callback: (PingState) -> Unit)
    fun deleteScheduledPing(id: String, callback: (PingState) -> Unit)
    fun getSentPings(fromUserId: String, offset: String?)
    fun getScheduledPingsWithPagination(fromUserId: String, offset: String?)
    fun loadReceivedPings(userId: String, offset: String)
    fun changePingSeenStatus(userId: String, pingId: String)

    fun addSentPingsListener(listener: NewPingsSentListener)
    fun removeSentPingsListener(listener: NewPingsSentListener)
    fun addScheduledPingsListener(listener: ScheduledPingsListener)
    fun removeScheduledPingsListener(listener: ScheduledPingsListener)

    fun addListener(listener: NewPingsListener, pingsType: PingsType)
    fun removeReceivedPingsValueListeners()
    fun removeSentPingsValueListeners()
    fun removeScheduledPingsValueListeners()
}