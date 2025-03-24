package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases

import com.khrd.pingapp.data.pings.DatabasePing
import kotlinx.coroutines.flow.StateFlow

interface GetSentPingsUseCase {
    fun getSentPings(): StateFlow<List<DatabasePing>?>
    fun loadMore()
}