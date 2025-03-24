package com.khrd.pingapp.firebase.authentication

sealed class ReauthenticationState

object ReauthenticationSuccess: ReauthenticationState()

data class ReauthenticationFailure(val error: ReauthenticationError): ReauthenticationState()

enum class ReauthenticationError {
    InvalidCredentials,
    NetworkFailure,
    TooManyRequests,
    UnknownFailure
}