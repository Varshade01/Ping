package com.khrd.pingapp.utils

import com.khrd.pingapp.homescreen.usecases.OnlineHandlerUseCase
import kotlinx.coroutines.*

class OnlineManagerImpl(
    val coroutineScope: CoroutineScope,
    val onlineHandlerUseCase: OnlineHandlerUseCase
) : OnlineManager {
    private var job: Job? = null

    override fun start() {
        job?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        job = coroutineScope.launch() {
            while (job?.isActive == true) {
                onlineHandlerUseCase.sendOnlineStatus(true)
                delay(60000)
            }
        }
    }

    override fun stop(currentUserId: String?) {
        job?.cancel()
        onlineHandlerUseCase.sendOnlineStatus(false, currentUserId)
    }
}