package com.khrd.pingapp.homescreen.usecases.jobposition

import com.khrd.pingapp.homescreen.states.JobNameValidationState
import com.khrd.pingapp.utils.validateOnlyLettersAndSpace

class JobNameValidationUseCaseImpl : JobNameValidationUseCase {
    override fun validate(name: String): JobNameValidationState {
        return if (name.isBlank()) {
            JobNameValidationState.VALID
        } else if (!validateOnlyLettersAndSpace(name)) {
            JobNameValidationState.INVALID_CHARS
        } else if (name.length > 64) {
            JobNameValidationState.TOO_LONG
        } else {
            JobNameValidationState.VALID
        }
    }
}