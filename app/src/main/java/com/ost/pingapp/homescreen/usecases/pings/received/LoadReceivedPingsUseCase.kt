package com.khrd.pingapp.homescreen.usecases.pings.received

import com.khrd.pingapp.repository.pings.ReceivedPingsData
import kotlinx.coroutines.flow.StateFlow

interface LoadReceivedPingsUseCase {
    fun loadReceivedPings(): StateFlow<ReceivedPingsData?>
    fun loadMoreReceivedPings()
}