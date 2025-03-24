package com.khrd.pingapp.homescreen.usecases

import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.repository.users.UsersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class RemoveFcmTokenUseCaseImpl @Inject constructor(
    private val userRepository: UsersRepository,
    private val dataStoreManager: DataStoreManager,
    private val fireBaseAuth: FirebaseAuthAPI,
    private val coroutineScope: CoroutineScope
) : RemoveFcmTokenUseCase {
    override fun removeFcmToken(currentUserId: String?) {
        coroutineScope.launch {
            val userId = currentUserId ?: fireBaseAuth.currentUserId()
            if (userId != null) {
                val token = dataStoreManager.getFcmToken()
                if (token.isNotEmpty()) {
                    userRepository.removeFcmToken(userId, token)
                }
            }
        }
    }
}