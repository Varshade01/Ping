package com.khrd.pingapp.homescreen.listeners

import com.khrd.pingapp.data.pings.DatabasePing

interface NewPingsSentListener {
    fun onNewSentPings(pings: List<DatabasePing>)
}