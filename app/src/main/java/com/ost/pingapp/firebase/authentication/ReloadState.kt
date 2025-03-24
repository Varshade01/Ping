package com.khrd.pingapp.firebase.authentication

sealed class ReloadState

object ReloadStateSuccess: ReloadState()

data class ReloadStateFailure(val error: ReloadError): ReloadState()

enum class ReloadError {
    NetworkFailure,
    InvalidUserFailure,
    UnknownFailure
}