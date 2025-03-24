package com.khrd.pingapp.homescreen.usecases.deleteprofilephoto

import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.repository.users.UsersRepository

class DeleteProfilePhotoUseCaseImpl(
    val usersRepository: UsersRepository
) : DeleteProfilePhotoUseCase {
    override fun deleteProfilePhoto(id: String, callback: (UserRequestState) -> Unit) {
        usersRepository.deleteProfilePhoto(id) { state -> callback(state) }
    }
}