package com.khrd.pingapp.homescreen.changePassword

sealed interface IUpdatePasswordState

sealed class UpdatePasswordState : IUpdatePasswordState

class UpdatePasswordSuccess : UpdatePasswordState()

class UpdatePasswordFailure(val error: UpdatePasswordErrors) : UpdatePasswordState()

enum class UpdatePasswordErrors {
    NO_INTERNET_CONNECTION,
    TOO_MANY_REQUESTS,
    UNKNOWN_ERROR,
    AUTHENTICATE_FAILURE,
    PASSWORDS_ARE_SAME
}