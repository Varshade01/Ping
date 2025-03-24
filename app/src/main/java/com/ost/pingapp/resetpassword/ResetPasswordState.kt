package com.khrd.pingapp.resetpassword

enum class ResetPasswordState {
    ResetPasswordSuccess,
    NetworkFailure,
    InvalidEmail,
    UnknownFailure,
    TooManyRequests
}