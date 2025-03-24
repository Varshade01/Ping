package com.khrd.pingapp.login

enum class LoginState {
    LoginSuccess,
    EmailNotVerified,
    InvalidCredentials,
    NetworkFailure,
    TooManyRequests,
    EmptyEmail,
    EmptyPassword,
    InvalidEmail,
    UnknownFailure
}