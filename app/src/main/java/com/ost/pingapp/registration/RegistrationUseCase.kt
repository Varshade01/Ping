package com.khrd.pingapp.registration

interface RegistrationUseCase {
   suspend fun registerUser(fields: RegistrationFields): IRegistrationState
}