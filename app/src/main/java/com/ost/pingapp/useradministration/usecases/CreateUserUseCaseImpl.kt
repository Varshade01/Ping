package com.khrd.pingapp.useradministration.usecases

import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.homescreen.usecases.UpdateFcmTokenUseCase
import com.khrd.pingapp.registration.IRegistrationState
import com.khrd.pingapp.registration.RegistrationSuccess
import com.khrd.pingapp.registration.RegistrationUnknownFailure
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.utils.OnlineManager
import javax.inject.Inject

class CreateUserUseCaseImpl @Inject constructor(
    private val userRepository: UsersRepository, var updateFcmTokenUseCase: UpdateFcmTokenUseCase, val onlineManager: OnlineManager
) : CreateUserUseCase {

    override suspend fun createUser(userId: String, email: String, name: String): IRegistrationState {
        val state = userRepository.createUser(userId, email, name)
        // switching from UserRequestState into RegistrationState
        return if (state is UserRequestState.UserRequestSuccess) {
            updateFcmTokenUseCase.updateFcmToken()
            onlineManager.start()
            RegistrationSuccess()
        } else {
            RegistrationUnknownFailure()
        }
    }
}
