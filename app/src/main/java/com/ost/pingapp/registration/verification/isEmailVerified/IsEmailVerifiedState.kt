package com.khrd.pingapp.registration.verification.isEmailVerified

sealed interface IEmailVerifiedState

sealed class IsEmailVerifiedState(): IEmailVerifiedState

data class IsEmailVerifiedSuccess(val result: Boolean): IsEmailVerifiedState()

class  IsEmailVerifiedNetworkFailure(): IsEmailVerifiedState()

class IsEmailVerifiedUnknownFailure(): IsEmailVerifiedState()
