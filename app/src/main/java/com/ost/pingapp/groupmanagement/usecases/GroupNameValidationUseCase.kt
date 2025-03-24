package com.khrd.pingapp.groupmanagement.usecases

import com.khrd.pingapp.groupmanagement.states.GroupNameValidationState

interface GroupNameValidationUseCase {
    fun validateName(name: String?): GroupNameValidationState
}