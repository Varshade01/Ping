package com.khrd.pingapp.homescreen.states

sealed class JoinByOutsideLinkState

object JoinByOutsideLinkSuccess: JoinByOutsideLinkState()

data class JoinByOutsideLinkFailure(val error: JoinByOutsideLinkError): JoinByOutsideLinkState()

enum class JoinByOutsideLinkError {
    UNEXISTING_GROUP,
    UNKNOWN_ERROR
}