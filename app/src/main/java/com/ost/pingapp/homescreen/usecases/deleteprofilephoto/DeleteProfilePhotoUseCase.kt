package com.khrd.pingapp.homescreen.usecases.deleteprofilephoto

import com.khrd.pingapp.data.users.UserRequestState

interface DeleteProfilePhotoUseCase {
    fun deleteProfilePhoto(id: String, callback: (UserRequestState) -> Unit)
}