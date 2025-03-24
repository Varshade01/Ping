package com.khrd.pingapp.homescreen.usecases.pings.received

import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.repository.pings.PingsRepository
import com.khrd.pingapp.repository.pings.ReceivedPingsData
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class LoadReceivedPingsUseCaseImpl @Inject constructor(
    val firebaseAuth: FirebaseAuthAPI,
    val pingsRepository: PingsRepository
) : LoadReceivedPingsUseCase {
    override fun loadReceivedPings(): StateFlow<ReceivedPingsData?> {
        return pingsRepository.loadReceivedPings(firebaseAuth.currentUserId())
    }

    override fun loadMoreReceivedPings() {
        pingsRepository.loadMoreReceivedPings(firebaseAuth.currentUserId())
    }
}