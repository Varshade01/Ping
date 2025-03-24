package com.khrd.pingapp.homescreen.usecases.updateprofilephoto

import com.khrd.pingapp.data.users.UserRequestState

interface UpdateProfilePhotoUseCase {
    fun updateProfilePhoto(id: String, bytes: ByteArray,callback: (UserRequestState) -> Unit)
}