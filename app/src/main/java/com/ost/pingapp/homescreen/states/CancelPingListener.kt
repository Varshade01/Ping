package com.khrd.pingapp.homescreen.states

interface CancelPingListener {
    fun onCancelPing(pingId: String, time: Long)
}