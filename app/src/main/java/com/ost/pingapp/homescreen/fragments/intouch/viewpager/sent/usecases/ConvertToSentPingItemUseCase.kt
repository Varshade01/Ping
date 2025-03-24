package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases

import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.viewholders.GetPingsState

interface ConvertToSentPingItemUseCase {
    fun convertToSentPing(pings: List<DatabasePing>, callback: (GetPingsState) -> Unit)
}