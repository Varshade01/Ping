package com.khrd.pingapp.registration.verification.sendEmail

sealed interface ISendConfirmationEmailState

sealed class SendConfirmationEmailState(): ISendConfirmationEmailState

class SendConfirmationEmailSuccess(): SendConfirmationEmailState()

class SendConfirmationEmailTooManyRequests(): SendConfirmationEmailState()

class  SendConfirmationEmailNetworkFailure(): SendConfirmationEmailState()

class SendConfirmationEmailUnknownFailure(): SendConfirmationEmailState()
