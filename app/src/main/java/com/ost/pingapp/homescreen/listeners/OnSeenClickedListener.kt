package com.khrd.pingapp.homescreen.listeners

import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingItem

interface OnSeenClickedListener {
    fun onSeenClicked(sentPingItem: SentPingItem)
}