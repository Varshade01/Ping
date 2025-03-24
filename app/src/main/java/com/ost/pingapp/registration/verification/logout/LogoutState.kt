package com.khrd.pingapp.registration.verification.logout

sealed interface ILogoutState

sealed class LogoutState(): ILogoutState

class LogoutSuccess(): LogoutState()

class  LogoutNetworkFailure(): LogoutState()

class LogoutUnknownFailure(): LogoutState()
