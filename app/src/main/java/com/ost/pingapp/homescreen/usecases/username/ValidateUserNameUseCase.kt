package com.khrd.pingapp.homescreen.usecases.username

import com.khrd.pingapp.homescreen.states.UserNameValidationState

interface ValidateUserNameUseCase {
    fun validateName(name: String?): UserNameValidationState
}