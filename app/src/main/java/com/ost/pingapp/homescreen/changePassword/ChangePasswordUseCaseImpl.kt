package com.khrd.pingapp.homescreen.changePassword

import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.authentication.ReauthenticationError
import com.khrd.pingapp.firebase.authentication.ReauthenticationFailure
import com.khrd.pingapp.firebase.authentication.ReauthenticationSuccess
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import javax.inject.Inject

class ChangePasswordUseCaseImpl @Inject constructor(
    val firebaseAuth: FirebaseAuthAPI,
    val firebaseConnectionStatus: ConnectionStatus
) : ChangePasswordUseCase {
    override fun changePassword(oldPassword: String, newPassword: String, callback: (UpdatePasswordState) -> Unit) {
        if (oldPassword == newPassword) {
            callback(UpdatePasswordFailure(UpdatePasswordErrors.PASSWORDS_ARE_SAME))
        } else {
            firebaseAuth.reauthenticate(oldPassword) { reauthState ->
                when (reauthState) {
                    ReauthenticationSuccess -> {
                        firebaseAuth.changePassword(newPassword) {
                            when (it) {
                                is UpdatePasswordSuccess -> callback(it)
                                is UpdatePasswordFailure -> callback(it)
                            }
                        }
                    }
                    is ReauthenticationFailure -> {
                        when (reauthState.error) {
                            ReauthenticationError.InvalidCredentials -> callback(UpdatePasswordFailure(UpdatePasswordErrors.AUTHENTICATE_FAILURE))
                            ReauthenticationError.NetworkFailure -> callback(UpdatePasswordFailure(UpdatePasswordErrors.NO_INTERNET_CONNECTION))
                            ReauthenticationError.TooManyRequests -> callback(UpdatePasswordFailure(UpdatePasswordErrors.TOO_MANY_REQUESTS))
                            ReauthenticationError.UnknownFailure -> callback(UpdatePasswordFailure(UpdatePasswordErrors.UNKNOWN_ERROR))
                        }
                    }
                }
            }
        }
    }
}