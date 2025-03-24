package com.khrd.pingapp.homescreen.listeners

import com.khrd.pingapp.data.pings.DatabasePing

interface ScheduledPingsListener {
    fun onNewPingsScheduledOrChanged(pings: List<DatabasePing>)
}