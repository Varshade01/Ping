package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases

import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.repository.pings.PingsRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetSentPingsUseCaseImpl @Inject constructor(
    private val pingsRepository: PingsRepository,
    private val firebaseAuth: FirebaseAuthAPI
) : GetSentPingsUseCase {
    override fun getSentPings(): StateFlow<List<DatabasePing>?> {
        return pingsRepository.getSentPings(firebaseAuth.currentUserId())
    }

    override fun loadMore() {
        pingsRepository.loadMoreSentPings(firebaseAuth.currentUserId())
    }
}
