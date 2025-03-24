package com.khrd.pingapp.groupmanagement.usecases

import com.khrd.pingapp.groupmanagement.states.GroupNameValidationState
import com.khrd.pingapp.utils.validateLetters

class GroupNameValidationUseCaseImpl : GroupNameValidationUseCase {
    override fun validateName(name: String?): GroupNameValidationState {
        if (name.isNullOrBlank()) {
            return GroupNameValidationState.EMPTY_FIELD
        } else if (name.length > 64) {
            return GroupNameValidationState.TOO_LONG
        } else if(!validateLetters(name)){
            return GroupNameValidationState.INVALID_CHARS
        }
        return GroupNameValidationState.VALID
    }
}