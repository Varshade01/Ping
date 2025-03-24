package com.khrd.pingapp.homescreen.usecases.jobposition

import com.khrd.pingapp.homescreen.states.JobNameValidationState

interface JobNameValidationUseCase {
    fun validate(name: String): JobNameValidationState
}