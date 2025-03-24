package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import kotlinx.coroutines.flow.StateFlow

interface GetScheduledPingsUseCase {
    fun getScheduledPings(dataLoadFlag: DataLoadFlag = DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE): StateFlow<List<DatabasePing>?>
    fun loadMoreScheduledPings()
}