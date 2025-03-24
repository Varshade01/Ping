package com.khrd.pingapp.homescreen.usecases.jobposition

import com.khrd.pingapp.data.users.UserRequestState

interface UpdateJobPositionUseCase {
    fun updateJobPosition(job: String, callback: (UserRequestState) -> Unit)
}