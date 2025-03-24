package com.khrd.pingapp.useradministration.usecases

import com.khrd.pingapp.registration.IRegistrationState

interface CreateUserUseCase {
   suspend fun createUser(userId: String, email: String, name: String): IRegistrationState
}