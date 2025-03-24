package com.khrd.pingapp.homescreen.usecases

import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.repository.users.UsersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UpdateFcmTokenUseCaseImpl(
    private val userRepository: UsersRepository,
    private val dataStoreManager: DataStoreManager,
    private val fireBaseAuth: FirebaseAuthAPI,
    private val coroutineScope: CoroutineScope
) : UpdateFcmTokenUseCase {
    override fun updateFcmToken(token: String?) {
        coroutineScope.launch {
            if (fireBaseAuth.currentUserId() != null) {
                if (!token.isNullOrEmpty()) {
                    userRepository.updateFcmToken(fireBaseAuth.currentUserId()!!, token)
                } else {
                    val dataStoreToken = dataStoreManager.getFcmToken()
                    if (dataStoreToken.isNotEmpty()) {
                        userRepository.updateFcmToken(fireBaseAuth.currentUserId()!!, dataStoreToken)
                    }
                }
            }
        }
    }
}

