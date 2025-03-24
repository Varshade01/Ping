package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases

import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.repository.pings.PingsRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetScheduledPingsUseCaseImpl @Inject constructor(
    private val pingsRepository: PingsRepository,
    private val firebaseAuth: FirebaseAuthAPI
) : GetScheduledPingsUseCase {
    override fun getScheduledPings(dataLoadFlag: DataLoadFlag): StateFlow<List<DatabasePing>?> {
        return pingsRepository.getScheduledPings(firebaseAuth.currentUserId(), dataLoadFlag)
    }

    override fun loadMoreScheduledPings() {
        pingsRepository.loadMoreScheduledPings(firebaseAuth.currentUserId())
    }
}