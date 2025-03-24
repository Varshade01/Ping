package com.khrd.pingapp.homescreen.states

sealed interface UpdateProfilePhotoState
data class UpdateProfilePhotoSuccess(val urlPhoto: String?) : UpdateProfilePhotoState
object UpdateProfilePhotoFail: UpdateProfilePhotoState