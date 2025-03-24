package com.khrd.pingapp.homescreen.usecases.username

import com.khrd.pingapp.utils.validateLetters
import com.khrd.pingapp.homescreen.states.UserNameValidationState

class ValidateUserNameUseCaseImpl : ValidateUserNameUseCase {
    override fun validateName(name: String?): UserNameValidationState {
        return if (name.isNullOrBlank()) {
            UserNameValidationState.EMPTY_FIELD
        } else if (!validateLetters(name)) {
            UserNameValidationState.NAME_LETTERS_FAILURE
        } else if (name.length > 32) {
            UserNameValidationState.TOO_LONG
        } else {
            UserNameValidationState.VALID
        }
    }
}