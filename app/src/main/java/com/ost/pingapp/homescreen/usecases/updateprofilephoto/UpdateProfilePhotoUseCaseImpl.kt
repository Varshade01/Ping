package com.khrd.pingapp.homescreen.usecases.updateprofilephoto

import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.repository.users.UsersRepository

class UpdateProfilePhotoUseCaseImpl(
   val usersRepository: UsersRepository
): UpdateProfilePhotoUseCase {
    override fun updateProfilePhoto(id: String, bytes: ByteArray, callback: (UserRequestState) -> Unit) {
        usersRepository.updateProfilePhoto(id, bytes){state -> callback(state)}
    }
}