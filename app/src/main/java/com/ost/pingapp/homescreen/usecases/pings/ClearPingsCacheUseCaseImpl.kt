package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.repository.pings.PingsRepository
import javax.inject.Inject

class ClearPingsCacheUseCaseImpl @Inject constructor(
    val pingsRepository: PingsRepository
) : ClearReceivedPingsCacheUseCase {
    override fun clearCache() {
        pingsRepository.clearReceivedPingsCache()
        pingsRepository.clearSentPingsCache()
        pingsRepository.clearScheduledPingsCache()
    }
}