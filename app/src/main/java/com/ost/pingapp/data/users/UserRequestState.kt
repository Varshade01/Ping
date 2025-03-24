package com.khrd.pingapp.data.users

sealed class UserRequestState {
    data class UserRequestSuccess(val user: DatabaseUser?) : UserRequestState()
    object UserRequestFail : UserRequestState()
    data class UserRequestOffline(val user: DatabaseUser?) : UserRequestState()
}