package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.ReceivedPingItem

interface LoadReceivedPingsState
data class LoadReceivedPingsSuccess(val pings: List<ReceivedPingItem>): LoadReceivedPingsState
object LoadReceivedPingsFailure: LoadReceivedPingsState