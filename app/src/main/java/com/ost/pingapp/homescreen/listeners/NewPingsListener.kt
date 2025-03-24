package com.khrd.pingapp.homescreen.listeners

import com.khrd.pingapp.data.pings.DatabasePing

abstract class NewPingsListener {
    private var currentCheckSum: Long = -1

    abstract fun onNewPingsReceived(pings: List<DatabasePing>)
    fun setNewPings(pings: List<DatabasePing>) {
        var newCheckSum: Long = 0
        pings.forEach {
            newCheckSum += it.hashCode()
        }
        if (currentCheckSum != newCheckSum) {
            onNewPingsReceived(pings)
            currentCheckSum = newCheckSum
        }
    }
}