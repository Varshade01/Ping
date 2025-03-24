package com.khrd.pingapp.registration

import com.khrd.pingapp.utils.passwordValidation.ValidationState

sealed interface IRegistrationState

sealed class RegistrationState(): IRegistrationState

class RegistrationSuccess(): RegistrationState()

data class RegistrationValidationFailure(val errors:  Set<ValidationState>): RegistrationState()

class RegistrationUserExistFailure(): RegistrationState()

class RegistrationEmailAlreadyInUseFailure(): RegistrationState()

class  RegistrationNetworkFailure(): RegistrationState()

class RegistrationUnknownFailure(): RegistrationState()