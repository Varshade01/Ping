package com.khrd.pingapp.homescreen.usecases.pings

import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.repository.pings.PingsRepository
import javax.inject.Inject

class ChangePingSeenStatusUseCaseImpl @Inject constructor(
    private val pingsRepository: PingsRepository
): ChangePingSeenStatusUseCase {
    override fun changePingSeenStatus(pingId: String) {
        pingsRepository.changePingSeenStatus(pingId)
    }
}