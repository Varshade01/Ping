package com.khrd.pingapp.homescreen.changeEmail

sealed class ChangeEmailState

object ChangeEmailSuccess : ChangeEmailState()

data class ChangeEmailValidationSuccess(val email: String) : ChangeEmailState()

data class ChangeEmailFailure(val error: ChangeEmailError) : ChangeEmailState()

enum class ChangeEmailError {
    EMPTY_PASSWORD_FIELD,
    EMPTY_EMAIL_FIELD,
    INVALID_EMAIL,
    SAME_EMAIL_ERROR,
    EMAIL_ALREADY_REGISTERED,
    NETWORK_ERROR,
    INVALID_PASSWORD,
    TOO_MANY_REQUESTS,
    UNKNOWN_ERROR
}