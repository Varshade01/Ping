package com.khrd.pingapp.groupmanagement.usecases

import com.khrd.pingapp.data.groups.GroupState

interface LinkValidationUseCase {
    fun validateLink(link: String, callback: (GroupState) -> Unit)
}