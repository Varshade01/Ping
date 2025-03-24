package com.khrd.pingapp.homescreen.deleteAccount

sealed class DeleteAccountState

object DeleteAccountSuccess : DeleteAccountState()

data class DeleteAccountFailure(val error: DeleteAccountError) : DeleteAccountState()

enum class DeleteAccountError {
    PASSWORD_IS_TOO_SHORT,
    PASSWORD_DOESNT_MATCH,
    CONNECTION_ERROR,
    EMPTY_FIELD,
    TOO_MANY_REQUESTS,
    UNKNOWN_ERROR
}